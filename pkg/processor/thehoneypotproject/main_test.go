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
package thehoneypotproject

import (
	"log"
	"net/http"
	"testing"

	"github.com/spf13/viper"

	"gitlab.com/qaclana/qaclana/pkg/processor"
)

func init() {
	viper.Set("thehoneypot-apikey", "abcdef") // valid for tests
}

func TestEnsureRegistration(t *testing.T) {
	l := processor.Get().List()
	if len(l) < 1 {
		t.Error("The thehoneypotproject processor hasn't been registered")
	}
}

func TestListedAddress(t *testing.T) {
	log.Printf("thehoneypot-apikey: %s", viper.GetString("thehoneypot-apikey"))

	req := &http.Request{RemoteAddr: "127.1.10.1"}
	o := processor.GetOutcome(req)
	if o != processor.BLOCK {
		t.Error("IP was expected to be listed")
	}
}

func TestNonListedAddress(t *testing.T) {
	req := &http.Request{RemoteAddr: "127.0.0.1"}
	o := processor.GetOutcome(req)
	if o != processor.NEUTRAL {
		t.Error("IP not expected to yield a result")
	}
}

func TestUnconfiguredAlsoWorks(t *testing.T) {
	viper.Set("thehoneypot-apikey", nil)

	req := &http.Request{RemoteAddr: "127.1.10.1"}
	o := processor.GetOutcome(req)
	if o != processor.NEUTRAL {
		t.Error("IP not expected to yield a result")
	}
}

func TestInvalidIP(t *testing.T) {
	req := &http.Request{RemoteAddr: "a.b.c.d"}
	o := processor.GetOutcome(req)
	if o != processor.NEUTRAL {
		t.Error("IP not expected to yield a result")
	}
}
