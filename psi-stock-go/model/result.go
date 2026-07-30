package model

import "time"

// CommonResult 统一响应结构
type CommonResult struct {
	Code      int         `json:"code"`
	Message   string      `json:"message"`
	Data      interface{} `json:"data,omitempty"`
	Success   bool        `json:"success"`
	Timestamp int64       `json:"timestamp"`
}

func Success(data interface{}) CommonResult {
	return CommonResult{
		Code:      200,
		Message:   "操作成功",
		Data:      data,
		Success:   true,
		Timestamp: time.Now().UnixMilli(),
	}
}

func Fail(code int, message string) CommonResult {
	return CommonResult{
		Code:      code,
		Message:   message,
		Success:   false,
		Timestamp: time.Now().UnixMilli(),
	}
}

// PageResult 分页结构
type PageResult struct {
	List     interface{} `json:"list"`
	Total    int64       `json:"total"`
	PageNum  int         `json:"pageNum"`
	PageSize int         `json:"pageSize"`
	Pages    int         `json:"pages"`
}
