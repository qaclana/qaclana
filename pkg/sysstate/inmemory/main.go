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

// Package inmemory implements an in-memory storage of the system state
package inmemory

import (
	"context"
	"log"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

// InMemory stores the system state in memory
type InMemory struct {

	// the current system state
	s qaclana.State

	// the parties to notify upon state changes
	ns []chan (qaclana.State)
}

// WithState constructs a new InMemory storage with the given state as initial state
func WithState(s qaclana.State) *InMemory {
	return &InMemory{s: s}
}

// Store the given state in memory
func (m *InMemory) Store(ctx context.Context, s qaclana.State) error {
	if m.s == s {
		log.Printf("Given state (%s) is the same as the current one (%s)", s, m.s)
		return nil
	}

	log.Printf("Setting state to %s", s)
	m.s = s
	log.Printf("Set state to %s", m.s)
	m.NotifyAll(s)
	return nil
}

// Notifier yields a channel used to notify state changes
func (m *InMemory) Notifier() (<-chan qaclana.State, error) {
	c := make(chan qaclana.State)
	m.ns = append(m.ns, c)
	return c, nil
}

// Current gets the currently set system state
func (m *InMemory) Current() (qaclana.State, error) {
	return m.s, nil
}

// NotifyAll propagates a state change to all registered notifiers
func (m *InMemory) NotifyAll(s qaclana.State) error {
	for _, n := range m.ns {
		n <- s
	}

	return nil
}
