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

package director

import (
	"net/http"
	"testing"

	opentracing "github.com/opentracing/opentracing-go"
	"github.com/opentracing/opentracing-go/mocktracer"
)

func TestTracingIntegration(t *testing.T) {
	// prepare
	tracer := mocktracer.New()
	opentracing.InitGlobalTracer(tracer)
	defer tracer.Reset()

	s := NewRoundTripper(nil)
	req := &http.Request{}

	// test
	s.RoundTrip(req)

	// spans:
	// - parent span
	// - request
	// - backend
	// - response
	if len(tracer.FinishedSpans()) != 4 {
		t.Fail()
	}
}
