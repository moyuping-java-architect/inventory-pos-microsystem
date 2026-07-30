package handler

import (
	"encoding/json"
	"io"
	"net/http"
	"strings"

	"github.com/psi/psi-stock-go/model"
	"github.com/psi/psi-stock-go/service"
)

// StockHandler 库存接口处理器
type StockHandler struct {
	svc *service.StockService
}

func NewStockHandler(svc *service.StockService) *StockHandler {
	return &StockHandler{svc: svc}
}

func (h *StockHandler) Register(mux *http.ServeMux) {
	mux.HandleFunc("/psi/stock/go/increase", h.Increase)
	mux.HandleFunc("/psi/stock/go/decrease", h.Decrease)
	mux.HandleFunc("/psi/stock/go/lock", h.Lock)
	mux.HandleFunc("/psi/stock/go/release", h.Release)
	mux.HandleFunc("/psi/stock/go/confirm", h.Confirm)
	mux.HandleFunc("/psi/stock/go/batch/decrease", h.BatchDecrease)
	mux.HandleFunc("/psi/stock/go/health", h.Health)
}

func (h *StockHandler) Increase(w http.ResponseWriter, r *http.Request) {
	var req model.StockOperateRequest
	if !bindJSON(w, r, &req) {
		return
	}
	if err := h.svc.Increase(req); err != nil {
		writeJSON(w, model.Fail(500, err.Error()))
		return
	}
	writeJSON(w, model.Success(nil))
}

func (h *StockHandler) Decrease(w http.ResponseWriter, r *http.Request) {
	var req model.StockOperateRequest
	if !bindJSON(w, r, &req) {
		return
	}
	if err := h.svc.Decrease(req); err != nil {
		writeJSON(w, model.Fail(500, err.Error()))
		return
	}
	writeJSON(w, model.Success(nil))
}

func (h *StockHandler) Lock(w http.ResponseWriter, r *http.Request) {
	var req model.StockOperateRequest
	if !bindJSON(w, r, &req) {
		return
	}
	if err := h.svc.Lock(req); err != nil {
		writeJSON(w, model.Fail(500, err.Error()))
		return
	}
	writeJSON(w, model.Success(nil))
}

func (h *StockHandler) Release(w http.ResponseWriter, r *http.Request) {
	var req model.StockOperateRequest
	if !bindJSON(w, r, &req) {
		return
	}
	if err := h.svc.Release(req); err != nil {
		writeJSON(w, model.Fail(500, err.Error()))
		return
	}
	writeJSON(w, model.Success(nil))
}

func (h *StockHandler) Confirm(w http.ResponseWriter, r *http.Request) {
	var req model.StockOperateRequest
	if !bindJSON(w, r, &req) {
		return
	}
	if err := h.svc.Confirm(req); err != nil {
		writeJSON(w, model.Fail(500, err.Error()))
		return
	}
	writeJSON(w, model.Success(nil))
}

func (h *StockHandler) BatchDecrease(w http.ResponseWriter, r *http.Request) {
	var req model.StockBatchOperateRequest
	if !bindJSON(w, r, &req) {
		return
	}
	if err := h.svc.BatchDecrease(req); err != nil {
		writeJSON(w, model.Fail(500, err.Error()))
		return
	}
	writeJSON(w, model.Success(nil))
}

func (h *StockHandler) Health(w http.ResponseWriter, r *http.Request) {
	writeJSON(w, model.Success("ok"))
}

func bindJSON(w http.ResponseWriter, r *http.Request, dst interface{}) bool {
	if r.Method != http.MethodPost {
		writeJSON(w, model.Fail(405, "仅支持 POST 请求"))
		return false
	}
	body, err := io.ReadAll(r.Body)
	if err != nil {
		writeJSON(w, model.Fail(400, "读取请求体失败"))
		return false
	}
	defer r.Body.Close()
	if err := json.Unmarshal(body, dst); err != nil {
		writeJSON(w, model.Fail(400, "参数错误: "+err.Error()))
		return false
	}
	return true
}

func writeJSON(w http.ResponseWriter, data interface{}) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(http.StatusOK)
	_ = json.NewEncoder(w).Encode(data)
}

// CORS 中间件
func CORS(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Tenant-Id, X-Shop-Id, X-Warehouse-Id")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next.ServeHTTP(w, r)
	})
}

// NotFoundHandler 404 返回 JSON
func NotFoundHandler(w http.ResponseWriter, r *http.Request) {
	if !strings.HasPrefix(r.URL.Path, "/psi/stock/go") {
		writeJSON(w, model.Fail(404, "not found"))
		return
	}
	writeJSON(w, model.Fail(404, "接口不存在"))
}
