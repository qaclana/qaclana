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
package sysstate

import (
	"log"
	"sync"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

type client chan<- qaclana.State

var (
	systemState qaclana.State
	entering    = make(chan client)
	leaving     = make(chan client)
	messages    = make(chan qaclana.State)

	mu = sync.Mutex{}
	co = sync.NewCond(&mu)
)

func StartBroadcaster() {
	mu.Lock()
	go broadcaster()
	co.Wait()
}

func Current() (qaclana.State, error) {
	return systemState, nil
}

func Set(s qaclana.State) error {
	systemState = s
	if DatabaseConfigured() {
		err := updateDatabase(s)
		if err != nil {
			return err
		}
	}

	go func() {
		log.Println("Processing system state change:", s)
		messages <- s
	}()
	return nil
}

func ListenForUpdates(cl chan qaclana.State) {
	entering <- cl
}

func broadcaster() {
	clients := make(map[client]bool)
	log.Println("Preparing the broadcaster")
	co.Broadcast()
	for {
		select {
		case state := <-messages:
			log.Println("Publishing system state change to clients")
			for cli := range clients {
				cli <- state
			}

		case cli := <-entering:
			clients[cli] = true

		case cli := <-leaving:
			log.Println("Client unregistered")
			delete(clients, cli)
			close(cli)
		}
	}

	log.Println("Finishing broadcaster")
}
