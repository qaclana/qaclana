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

package processor

import (
	"context"
	"log"
	"net/http"
	"time"
)

var r = &Registry{}

// Registry stores all the processors known to this application
type Registry struct {
	ps []Processor
}

func (r *Registry) register(p Processor) {
	r.ps = append(r.ps, p)
}

// List the registered processors
func (r *Registry) List() []Processor {
	return r.ps
}

// Get the processor registry
func Get() *Registry {
	return r
}

// Register a new processor
func Register(p Processor) {
	log.Printf("Registering the processor %s", p)
	r.register(p)
}

// GetOutcome for the given request, stopping after the first non-neutral outcome
func GetOutcome(req *http.Request) Outcome {
	timeout := 50 * time.Millisecond
	ctx, cancel := context.WithTimeout(req.Context(), timeout)

	for _, p := range Get().List() {
		o, err := p.Process(req.WithContext(ctx))
		if err != nil {
			log.Printf("registry: %s", err)
		}

		if o != NEUTRAL {
			log.Printf("outcome: %d", o)
			cancel()
			return o
		}
	}

	cancel()
	return NEUTRAL
}
