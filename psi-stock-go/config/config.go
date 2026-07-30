package config

import (
	"os"
	"strconv"
)

// AppConfig 应用配置
type AppConfig struct {
	ServerPort  int
	ServerName  string
	DBHost      string
	DBPort      int
	DBUser      string
	DBPassword  string
	DBName      string
	RedisHost   string
	RedisPort   int
	RedisDB     int
	RedisPass   string
	NacosHost   string
	NacosPort   uint64
	NacosNS     string
	NacosGroup  string
	NacosDataID string
}

func Load() *AppConfig {
	return &AppConfig{
		ServerPort:  getEnvInt("SERVER_PORT", 8093),
		ServerName:  getEnv("SERVER_NAME", "psi-stock-go"),
		DBHost:      getEnv("DB_HOST", "localhost"),
		DBPort:      getEnvInt("DB_PORT", 3306),
		DBUser:      getEnv("DB_USER", "root"),
		DBPassword:  getEnv("DB_PASSWORD", "123456"),
		DBName:      getEnv("DB_NAME", "psi_stock"),
		RedisHost:   getEnv("REDIS_HOST", "localhost"),
		RedisPort:   getEnvInt("REDIS_PORT", 6379),
		RedisDB:     getEnvInt("REDIS_DB", 0),
		RedisPass:   getEnv("REDIS_PASSWORD", ""),
		NacosHost:   getEnv("NACOS_HOST", "127.0.0.1"),
		NacosPort:   getEnvUint64("NACOS_PORT", 8848),
		NacosNS:     getEnv("NACOS_NAMESPACE", ""),
		NacosGroup:  getEnv("NACOS_GROUP", "DEFAULT_GROUP"),
		NacosDataID: getEnv("NACOS_DATA_ID", "psi-stock-go.yml"),
	}
}

func getEnv(key, defaultVal string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return defaultVal
}

func getEnvInt(key string, defaultVal int) int {
	v := os.Getenv(key)
	if v == "" {
		return defaultVal
	}
	i, err := strconv.Atoi(v)
	if err != nil {
		return defaultVal
	}
	return i
}

func getEnvUint64(key string, defaultVal uint64) uint64 {
	v := os.Getenv(key)
	if v == "" {
		return defaultVal
	}
	i, err := strconv.ParseUint(v, 10, 64)
	if err != nil {
		return defaultVal
	}
	return i
}
