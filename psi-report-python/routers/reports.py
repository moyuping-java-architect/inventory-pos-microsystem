from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from typing import Optional

from models.report import ReportRequest
from services import report_generator

router = APIRouter()


@router.post("/generate")
def generate_report(request: ReportRequest):
    """
    生成报表并返回文件流
    """
    try:
        content, filename, content_type = report_generator.generate(request)
        return StreamingResponse(
            iter([content]),
            media_type=content_type,
            headers={"Content-Disposition": f"attachment; filename=\"{filename}\""}
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"报表生成失败: {str(e)}")
