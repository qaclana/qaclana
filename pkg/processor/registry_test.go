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
	"fmt"
	"net/http"
	"testing"
)

func TestEnsureNoRegistration(t *testing.T) {
	if len(Get().List()) != 0 {
		t.Error("There should have been no registration")
	}
}

func TestAllNeutrals(t *testing.T) {
	Register(&neutralAll{})

	if len(Get().List()) != 1 {
		t.Error("There should be a processor registered")
	}

	o := GetOutcome(&http.Request{})
	if o != NEUTRAL {
		t.Error("The processor should have ended with a NEUTRAL outcome")
	}

	r.ps = nil
}

func TestBlockRequest(t *testing.T) {
	Register(&neutralAll{})
	Register(&blockAll{})

	if len(Get().List()) != 2 {
		t.Error("There should be 2 processors registered")
	}

	o := GetOutcome(&http.Request{})
	if o != BLOCK {
		t.Error("The processor should have ended with a BLOCK outcome")
	}

	r.ps = nil
}

func TestAllowRequest(t *testing.T) {
	Register(&neutralAll{})
	Register(&allowAll{})

	if len(Get().List()) != 2 {
		t.Error("There should be 2 processors registered")
	}

	o := GetOutcome(&http.Request{})
	if o != ALLOW {
		t.Error("The processor should have ended with an ALLOW outcome")
	}

	r.ps = nil
}

func TestDeterminingProcessorFirst(t *testing.T) {
	Register(&allowAll{})
	Register(&neutralAll{})

	if len(Get().List()) != 2 {
		t.Error("There should be 2 processors registered")
	}

	o := GetOutcome(&http.Request{})
	if o != ALLOW {
		t.Error("The processor should have ended with an ALLOW outcome")
	}

	r.ps = nil
}

func TestDontRunOtherProcessors(t *testing.T) {
	np := &neutralAll{}
	Register(&allowAll{})
	Register(np)

	if len(Get().List()) != 2 {
		t.Error("There should be 2 processors registered")
	}

	o := GetOutcome(&http.Request{})
	if o != ALLOW {
		t.Error("The processor should have ended with an ALLOW outcome")
	}

	if np.called() {
		t.Error("The second processor should not have run after a first 'determining' processor finished running.")
	}

	r.ps = nil
}

func TestErrors(t *testing.T) {
	var tests = []struct {
		errorOutcome    Outcome
		expectedOutcome Outcome
		secondProcessor Processor
	}{
		{NEUTRAL, ALLOW, &allowAll{}},
		{ALLOW, ALLOW, &blockAll{}},
		{BLOCK, BLOCK, &allowAll{}},
	}

	for _, test := range tests {
		ep := &errorAll{o: test.errorOutcome, e: fmt.Errorf("Oops")}
		Register(ep)
		Register(test.secondProcessor)

		if len(Get().List()) != 2 {
			t.Error("There should be 2 processors registered")
		}

		o := GetOutcome(&http.Request{})
		if o != test.expectedOutcome {
			t.Errorf("The processor should have ended with a %d outcome when the error outcome is %d", test.expectedOutcome, test.errorOutcome)
		}

		r.ps = nil
	}
}

type allowAll struct{}
type blockAll struct{}
type neutralAll struct {
	c bool
}
type errorAll struct {
	e error
	o Outcome
}

func (p *blockAll) Process(req *http.Request) (Outcome, error) {
	return BLOCK, nil
}

func (p *errorAll) Process(req *http.Request) (Outcome, error) {
	return p.o, p.e
}

func (p *allowAll) Process(req *http.Request) (Outcome, error) {
	return ALLOW, nil
}

func (p *neutralAll) Process(req *http.Request) (Outcome, error) {
	p.c = true
	return NEUTRAL, nil
}

func (p *neutralAll) called() bool {
	return p.c
}
