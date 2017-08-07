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
package sysstate_test

import (
	"log"
	"testing"
	"time"

	"gitlab.com/qaclana/qaclana/pkg/backend/sysstate"
	"gitlab.com/qaclana/qaclana/pkg/proto"
)

func TestSetSystemState(t *testing.T) {
	// setup
	done := make(chan bool)
	sysstate.StartBroadcaster()

	// then, possibly in a go routine, create a channel and use it for listening to updates
	c := make(chan qaclana.State)
	sysstate.ListenForUpdates(c)
	go func() {
		select {
		case state := <-c:
			close(done)
			// verify
			if state != qaclana.State_ENFORCING {
				log.Println("An update has been received on gofunc 1", state)
			}
		}
	}()

	// just a sanity check:
	s, err := sysstate.Current()
	if s != qaclana.State_DISABLED || err != nil {
		t.Fail()
	}

	// test
	sysstate.Set(qaclana.State_ENFORCING)

	// verify
	select {
	case <-done:
	case <-time.After(1 * time.Second):
		log.Println("Test timed out without a response.")
		t.Fail()
	}
}
