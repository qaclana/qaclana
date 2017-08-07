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
	"database/sql"
	"errors"
	"log"
	"time"

	// Imports the Driver, no need to directly consume it
	_ "github.com/lib/pq"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

var (
	DatabaseUrl           string
	noRecordError         = errors.New("no record available")
	noConnectionAvailable = errors.New("no database connection information available")
)

func init() {
	go func() {
		// TODO: this has the potential to blow out of proportion: limit the concurrency and establish a timeout
		tick := time.Tick(time.Second / 2)
		for {
			select {
			case <-tick:
				go poll()
			}
		}
	}()
}

func poll() {
	state, _ := Current()
	fromDatabase, err := get()
	if err == nil && state != fromDatabase {
		log.Println("detected new system state: ", fromDatabase)
		Set(fromDatabase)
	}
}

func updateDatabase(s qaclana.State) error {
	fromDatabase, currentStateErr := get()
	if s != fromDatabase {
		if currentStateErr != noRecordError {
			// something else happened, do nothing
			return currentStateErr
		}

		db, err := sql.Open("postgres", DatabaseUrl)
		if err != nil {
			log.Println("error connecting to the database: ", err)
			return err
		}
		defer db.Close()

		// TODO: optimize this, to prepare only once (if that's how it's done in Go)
		var stmt *sql.Stmt
		if currentStateErr == noRecordError {
			log.Println("inserting new record")
			stmt, err = db.Prepare("INSERT INTO kv (key, value) VALUES('system-state', $1) ")
		} else {
			log.Println("updating existing record")
			stmt, err = db.Prepare("UPDATE kv SET value = $1 WHERE key = 'system-state' ")
		}
		defer stmt.Close()

		if err != nil {
			log.Println("error while preparing the statement to update the record on the database: ", err)
			return err
		}

		_, err = stmt.Exec(s)
		if err != nil {
			log.Println("error while updating the record on the database", err)
			return err
		}
	}

	return nil
}

func get() (qaclana.State, error) {
	if DatabaseUrl == "" {
		return qaclana.State_DISABLED, noConnectionAvailable
	}

	db, err := sql.Open("postgres", DatabaseUrl)
	if err != nil {
		log.Println("error connecting to the database: ", err)
		return qaclana.State_DISABLED, err
	}
	defer db.Close()

	rows, err := db.Query("SELECT value FROM kv where key = 'system-state' ")
	if err != nil {
		log.Println(err)
		return qaclana.State_DISABLED, err
	}
	defer rows.Close()

	for rows.Next() {
		var value int
		if err := rows.Scan(&value); err != nil {
			log.Println(err)
			return qaclana.State_DISABLED, err
		}

		return qaclana.State(value), nil
	}

	return qaclana.State_DISABLED, noRecordError
}

func DatabaseConfigured() bool {
	return DatabaseUrl != ""
}
