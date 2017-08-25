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
package database

import (
	"context"
	"database/sql"
	"errors"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

// Database is a storage that persists the system state into an underlying RDBMS
type Database struct {
	// the underlying database to retrieve/store the data to/from
	db *sql.DB

	// the parties to notify upon state changes
	ns []chan (qaclana.State)

	// the local cache for the current value
	current *qaclana.State
}

var (
	errNoRecord = errors.New("no record available")
)

// WithDB creates a new database storage object connecting to the database of type 'stype' at the given url
func WithDB(db *sql.DB) *Database {
	d := &Database{db: db}
	return d
}

// Store a new system state into the database
func (d *Database) Store(ctx context.Context, s qaclana.State) error {
	fromDatabase, currentStateErr := d.Current()
	if s != fromDatabase {
		if currentStateErr != errNoRecord && currentStateErr != nil {
			// something else happened, do nothing
			return currentStateErr
		}

		d.db.ExecContext(ctx, "UPSERT INTO kv (key, value) VALUES('system-state', $1) ", s)
		return d.notifyAll(s)
	}

	return nil
}

// Notifier yields a channel used to notify state changes
func (d *Database) Notifier() (<-chan qaclana.State, error) {
	c := make(chan qaclana.State)
	d.ns = append(d.ns, c)
	return c, nil
}

// Current gets the currently set system state
func (d *Database) Current() (qaclana.State, error) {
	rows, err := d.db.Query("SELECT value FROM kv where key = 'system-state' ")
	if err != nil {
		return qaclana.State_DISABLED, err
	}
	defer rows.Close()

	for rows.Next() {
		var value int
		rows.Scan(&value)
		return qaclana.State(value), nil
	}

	return qaclana.State_DISABLED, errNoRecord
}

// notifyAll propagates a state change to all registered notifiers
func (d *Database) notifyAll(s qaclana.State) error {
	for _, n := range d.ns {
		n <- s
	}

	return nil
}
