from typing import Any, Optional
from pydantic import BaseModel


class ReportRequest(BaseModel):
    """报表生成请求"""
    report_type: str
    format: str = "excel"  # excel 或 pdf
    params: Optional[dict[str, Any]] = None
    data: Optional[list[dict[str, Any]]] = None
