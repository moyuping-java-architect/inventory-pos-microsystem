package main

import (
	"context"
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/redis/go-redis/v9"
	_ "github.com/go-sql-driver/mysql"
	"github.com/psi/psi-stock-go/config"
	"github.com/psi/psi-stock-go/handler"
	psiNacos "github.com/psi/psi-stock-go/nacos"
	"github.com/psi/psi-stock-go/repository"
	"github.com/psi/psi-stock-go/service"
)

func main() {
	cfg := config.Load()

	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&parseTime=True&loc=Local",
		cfg.DBUser, cfg.DBPassword, cfg.DBHost, cfg.DBPort, cfg.DBName)
	db, err := sql.Open("mysql", dsn)
	if err != nil {
		log.Fatalf("数据库连接失败: %v", err)
	}
	defer db.Close()
	db.SetMaxOpenConns(100)
	db.SetMaxIdleConns(20)

	// Redis 客户端
	rdb := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", cfg.RedisHost, cfg.RedisPort),
		Password: cfg.RedisPass,
		DB:       cfg.RedisDB,
	})
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := rdb.Ping(ctx).Err(); err != nil {
		log.Fatalf("Redis 连接失败: %v", err)
	}
	log.Println("Redis 连接成功")

	repo := repository.NewStockRepository(db)
	redisRepo := repository.NewRedisStockRepository(rdb)
	svc := service.NewStockService(db, repo, redisRepo)
	stockHandler := handler.NewStockHandler(svc)

	mux := http.NewServeMux()
	stockHandler.Register(mux)
	mux.HandleFunc("/", handler.NotFoundHandler)

	// Nacos 注册与配置监听
	var nacosClient *psiNacos.Client
	if cfg.NacosHost != "" {
		nacosClient = psiNacos.NewClient(cfg.NacosHost, cfg.NacosPort, cfg.NacosNS, cfg.NacosDataID, cfg.NacosGroup, cfg.ServerName, cfg.ServerPort)
		if err := nacosClient.RegisterService(); err != nil {
			log.Printf("Nacos 服务注册失败: %v", err)
		}
		nacosClient.ListenConfig(func(content string) {
			enabled := psiNacos.ParseEnabled(content)
			log.Printf("Nacos 配置更新: go.enabled=%v", enabled)
		})
	}

	// 优雅退出
	go func() {
		sig := make(chan os.Signal, 1)
		signal.Notify(sig, syscall.SIGINT, syscall.SIGTERM)
		<-sig
		if nacosClient != nil {
			_ = nacosClient.DeregisterService()
		}
		_ = db.Close()
		_ = rdb.Close()
		os.Exit(0)
	}()

	addr := fmt.Sprintf(":%d", cfg.ServerPort)
	log.Printf("psi-stock-go 启动成功，监听 %s", addr)
	server := &http.Server{
		Addr:    addr,
		Handler: handler.CORS(mux),
	}
	if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatalf("服务启动失败: %v", err)
	}
}
