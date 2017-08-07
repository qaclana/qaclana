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
package handler

import (
	"fmt"
	"net/http"

	"gitlab.com/qaclana/qaclana/pkg/backend/sysstate"
	"gitlab.com/qaclana/qaclana/pkg/proto"
)

func RootHttpHandler(w http.ResponseWriter, _ *http.Request) {
	fmt.Fprintln(w, "OK")
}

func SystemStateHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		st, _ := sysstate.Current()
		fmt.Fprintln(w, st)
		return
	}

	if r.Method == "PUT" {
		sysstate.Set(qaclana.State_ENFORCING)
		st, _ := sysstate.Current()
		fmt.Fprintln(w, st)
		return
	}

	fmt.Fprintln(w, "Sorry, I don't know what you want to do...")
}
