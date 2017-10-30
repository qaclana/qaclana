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

// Package testutils provide a simple harness suite for ensuring conformance by implementations
package testutils

import (
	"context"
	"log"
	"sync"
	"testing"

	"gitlab.com/qaclana/qaclana/pkg/proto"
	"gitlab.com/qaclana/qaclana/pkg/sysstate"
)

// SimpleUpdate does a simple storage of a system state
func SimpleUpdate(t *testing.T, s sysstate.Storage) {
	err := s.Store(context.Background(), qaclana.State_ENFORCING)
	if err != nil {
		t.Errorf("Error not expected: %s", err)
	}
}

// StoreCallsNotifiers ensures the registered notifiers are called
func StoreCallsNotifiers(t *testing.T, s sysstate.Storage) {
	c, _ := s.Notifier()
	wg := &sync.WaitGroup{}
	wgStored := &sync.WaitGroup{}

	wg.Add(1)
	go func() {
		wg.Done()
		select {
		case s := <-c:
			if s != qaclana.State_ENFORCING {
				t.Errorf("Expected the enforcing state, got %s", s)
			}
		}
	}()

	wgStored.Add(1)
	go func() {
		wg.Wait() // wait until we are listening on the channel
		err := s.Store(context.Background(), qaclana.State_ENFORCING)
		if err != nil {
			t.Errorf("test error: %s", err)
		}
		wgStored.Done()
	}()

	wgStored.Wait()
	state, _ := s.Current()
	if state != qaclana.State_ENFORCING {
		t.Errorf("Expected the enforcing state, got %s", s)
	}
}

// MultipleChanges makes sure we get notifications for multiple changes
func MultipleChanges(t *testing.T, s sysstate.Storage) {
	c, _ := s.Notifier()
	wg := &sync.WaitGroup{}
	wgStored := &sync.WaitGroup{}

	wg.Add(1)
	wgStored.Add(3)
	go func() {
		wg.Done()
		for {
			select {
			case s := <-c:
				log.Printf("Got %s as update", s)
				wgStored.Done()
			}
		}
	}()

	go func() {
		wg.Wait() // wait until we are listening on the channel
		s.Store(context.Background(), qaclana.State_ENFORCING)
		s.Store(context.Background(), qaclana.State_DISABLED)
		s.Store(context.Background(), qaclana.State_PERMISSIVE)
		s.Store(context.Background(), qaclana.State_PERMISSIVE)
	}()

	wgStored.Wait()
}
