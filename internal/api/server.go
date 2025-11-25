package api

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/SethCurry/mooshroom/internal/models"
	"github.com/julienschmidt/httprouter"
	"go.uber.org/zap"
)

type ServerOption func(*Server)

type RequestContext struct {
	Writer  http.ResponseWriter
	Request *http.Request
	Params  httprouter.Params
	Logger  *zap.Logger
	DB      *models.Client
}

func (r *RequestContext) Context() context.Context {
	return r.Request.Context()
}

func (r *RequestContext) JSONResponse(status int, body interface{}) error {
	marshalled, err := json.Marshal(body)
	if err != nil {
		return fmt.Errorf("failed to marshal JSON: %w", err)
	}

	r.Writer.Header().Set("Content-Type", "application/json")
	r.Writer.WriteHeader(status)
	r.Writer.Write(marshalled)
	return nil
}

type Handler func(ctx *RequestContext) error

func WithLogger(logger *zap.Logger) ServerOption {
	return func(s *Server) {
		s.logger = logger
	}
}

func NewServer(db *models.Client, opts ...ServerOption) *Server {
	srv := &Server{
		router: httprouter.New(),
		logger: zap.NewNop(),
		db:     db,
	}

	for _, opt := range opts {
		opt(srv)
	}

	return srv
}

type Server struct {
	router *httprouter.Router
	logger *zap.Logger
	db     *models.Client
}

func (s *Server) Handle(method, path string, handler Handler) {
	s.router.Handle(method, path, func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		err := handler(&RequestContext{
			Writer:  w,
			Request: r,
			Params:  ps,
			Logger:  s.logger,
			DB:      s.db,
		})
		if err != nil {
			s.logger.Error("handler returned error", zap.Error(err))
			http.Error(w, err.Error(), http.StatusInternalServerError)
		}
	})
}

func (s *Server) RawHandler(method string, path string, handler http.Handler) {
	s.router.Handle(method, path, func(w http.ResponseWriter, r *http.Request, pq httprouter.Params) {
		handler.ServeHTTP(w, r)
	})
}

func (s *Server) ListenAndServe(listenAddr string) error {
	return http.ListenAndServe(listenAddr, s.router)
}
