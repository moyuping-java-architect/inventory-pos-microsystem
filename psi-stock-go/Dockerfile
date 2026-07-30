FROM golang:1.24-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
ENV GOPROXY=https://goproxy.cn,https://goproxy.io,https://proxy.golang.org,direct
RUN go mod download
COPY . .
RUN go build -o stock-go .

FROM alpine:latest
WORKDIR /app
COPY --from=builder /app/stock-go .
EXPOSE 8093
ENTRYPOINT ["./stock-go"]
