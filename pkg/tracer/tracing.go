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

package tracer

import (
	"io"
	"log"

	jaeger "github.com/uber/jaeger-client-go"
	jaegercfg "github.com/uber/jaeger-client-go/config"
)

// Tracing makes the bridge between the OpenTracing implementation and Qaclana components
type Tracing struct {
	config *jaegercfg.Configuration
	closer io.Closer
}

// NewTracing creates a new instance of the Tracing with a default configuration
func NewTracing() *Tracing {
	config := &jaegercfg.Configuration{
		Sampler: &jaegercfg.SamplerConfig{
			Type:  jaeger.SamplerTypeConst,
			Param: 1,
		},
	}
	t := WithConfig(config)
	t.Start()
	return t
}

// WithConfig creates a new Tracer with the given configuration
func WithConfig(config *jaegercfg.Configuration) *Tracing {
	return &Tracing{config: config}
}

// Start the backing tracer based on the current state
func (t *Tracing) Start() *Tracing {
	closer, err := t.config.InitGlobalTracer("qaclana")
	if err != nil {
		log.Printf("Could not initialize jaeger tracer: %s", err.Error())
		return t
	}
	t.closer = closer
	return t
}

// Close the tracing component
func (t *Tracing) Close() {
	t.closer.Close()
}
