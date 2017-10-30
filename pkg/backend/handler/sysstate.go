// Copyright © 2017 The Qaclana Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Package handler has a set of handlers for different purposes, like gRPC and regular HTTP
package handler

import (
	"fmt"
	"net/http"

	"gitlab.com/qaclana/qaclana/pkg/proto"
	"gitlab.com/qaclana/qaclana/pkg/sysstate"
)

// SysState represents the system state HTTP handler
type SysState struct {
	storage sysstate.Storage
}

// NewSysStateHandler creates a new system state HTTP handler
func NewSysStateHandler(storage sysstate.Storage) *SysState {
	return &SysState{storage: storage}
}

func (s *SysState) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		st, _ := s.storage.Current()
		fmt.Fprintln(w, st)
		return
	}

	if r.Method == "PUT" {
		s.storage.Store(r.Context(), qaclana.State_ENFORCING)
		st, _ := s.storage.Current()
		fmt.Fprintln(w, st)
		return
	}

	w.WriteHeader(http.StatusBadRequest)
	fmt.Fprintln(w, "Sorry, I don't know what you want to do...")
}
