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
package whitelist

import (
	"net"
	"net/http"
	"testing"

	"gitlab.com/qaclana/qaclana/pkg/processor"
)

func TestEnsureRegistration(t *testing.T) {
	l := processor.Get().List()
	if len(l) < 1 {
		t.Error("The whitelist processor hasn't been registered")
	}
}

func TestListedAddress(t *testing.T) {
	_, net, _ := net.ParseCIDR("192.168.2.10/24")
	p.Add(net)

	req := &http.Request{RemoteAddr: "192.168.2.10:345"}
	o := processor.GetOutcome(req)
	if o != processor.ALLOW {
		t.Error("IP was expected to be on the whitelist")
	}
}

func TestUnlistedAddress(t *testing.T) {
	_, net, _ := net.ParseCIDR("192.168.2.10/24")
	p.Add(net)

	req := &http.Request{RemoteAddr: "192.168.3.1:345"}
	o := processor.GetOutcome(req)
	if o != processor.NEUTRAL {
		t.Error("IP was not expected to be on the whitelist")
	}
}

func TestNoPort(t *testing.T) {
	_, net, _ := net.ParseCIDR("192.168.2.10/24")
	p.Add(net)

	req := &http.Request{RemoteAddr: "192.168.2.10"}
	o := processor.GetOutcome(req)
	if o != processor.ALLOW {
		t.Error("IP was expected to be on the whitelist")
	}
}

func TestInvalidIP(t *testing.T) {
	_, net, _ := net.ParseCIDR("192.168.2.10/24")
	p.Add(net)

	req := &http.Request{RemoteAddr: "256.168.2.10"}
	o := processor.GetOutcome(req)
	if o != processor.NEUTRAL {
		t.Error("The processor should have no effect on the outcome")
	}
}
