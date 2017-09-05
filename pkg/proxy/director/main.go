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
	"context"
	"net/http"

	"gitlab.com/qaclana/qaclana/pkg/processor"

	opentracing "github.com/opentracing/opentracing-go"
)

// SecurityRoundTripper wraps a request/response cycle, interrupting before the request
// a backend or before a response is sent to the client
type SecurityRoundTripper struct {
	wrapped http.RoundTripper
}

// NewRoundTripper creates a new instance of Qaclana's security RoundTripper
func NewRoundTripper(previous http.RoundTripper) *SecurityRoundTripper {
	if previous == nil {
		previous = http.DefaultTransport
	}
	return &SecurityRoundTripper{wrapped: previous}
}

// RoundTrip wraps the request/response cycle around the underlying RoundTripper, checking the
// response data before calling the backend and checking the response before sending back to the client
func (s *SecurityRoundTripper) RoundTrip(req *http.Request) (*http.Response, error) {
	sp, ctx := opentracing.StartSpanFromContext(req.Context(), "protect")
	defer sp.Finish()
	req = req.WithContext(ctx)

	outcome := s.checkRequest(req)
	if outcome == processor.BLOCK {
		// TODO: fill the response with an error message
	}

	res, err := s.doRoundTrip(req)

	outcome = s.checkResponse(req.Context(), res)
	if outcome == processor.BLOCK {
		// TODO: fill the response with an error message
	}

	// if we reached this part, it's all OK!
	return res, err
}

func (s *SecurityRoundTripper) doRoundTrip(req *http.Request) (*http.Response, error) {
	sp, _ := opentracing.StartSpanFromContext(req.Context(), "backend")
	defer sp.Finish()
	return s.wrapped.RoundTrip(req)
}

func (s *SecurityRoundTripper) checkRequest(req *http.Request) processor.Outcome {
	sp, ctx := opentracing.StartSpanFromContext(req.Context(), "request-filters")
	defer sp.Finish()

	outcome := processor.GetOutcome(req.WithContext(ctx))
	if outcome == processor.BLOCK {
		sp.SetTag("error", true)
		sp.SetTag("phase", "request")
	}

	sp.SetTag("request-outcome", outcome.String())
	return outcome
}

func (s *SecurityRoundTripper) checkResponse(ctx context.Context, res *http.Response) processor.Outcome {
	sp, _ := opentracing.StartSpanFromContext(ctx, "response-filters")
	defer sp.Finish()

	outcome := processor.NEUTRAL
	sp.SetTag("response-outcome", outcome.String())

	return outcome
}
