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
	"flag"
	"fmt"
	"net/url"
	"testing"

	"gitlab.com/qaclana/qaclana/pkg/sysstate"

	"github.com/cockroachdb/cockroach-go/testserver"

	"gitlab.com/qaclana/qaclana/pkg/proto"
	"gitlab.com/qaclana/qaclana/pkg/sysstate/testutils"
)

func TestWithDatabase(t *testing.T) {
	stop, url := db(t)
	defer stop()

	t.Run("SimpleUpdate", func(t *testing.T) {
		url.Path = "SimpleUpdate"
		db, _ := sql.Open("postgres", url.String())
		schema(t, db, "SimpleUpdate")
		d := WithDB(db)
		testutils.SimpleUpdate(t, d)
	})
	t.Run("StoreCallsNotifiers", func(t *testing.T) {
		url.Path = "StoreCallsNotifiers"
		db, _ := sql.Open("postgres", url.String())
		schema(t, db, "StoreCallsNotifiers")
		d := WithDB(db)
		testutils.StoreCallsNotifiers(t, d)
	})
	t.Run("MultipleChanges", func(t *testing.T) {
		url.Path = "MultipleChanges"
		db, _ := sql.Open("postgres", url.String())
		schema(t, db, "MultipleChanges")
		d := WithDB(db)
		testutils.MultipleChanges(t, d)
	})
	t.Run("TestNoSchema", func(t *testing.T) {
		url.Path = "NoSchema"
		db, _ := sql.Open("postgres", url.String())
		d := WithDB(db)
		testNoSchema(t, d)
	})
	t.Run("", func(t *testing.T) {
		db, _ := sql.Open("postgres", "")
		d := WithDB(db)
		testGetOffline(t, d)
	})
}

func testNoSchema(t *testing.T, d sysstate.Storage) {
	s, err := d.Current()
	if s != qaclana.State_DISABLED {
		t.Errorf("unexpected state: %s", s)
	}

	if err == nil {
		t.Errorf("expected error, but didn't fail!")
	}

	err = d.Store(context.Background(), qaclana.State_ENFORCING)
	if err == nil {
		t.Errorf("expected error, but didn't fail!")
	}
}

func testGetOffline(t *testing.T, d sysstate.Storage) {
	s, err := d.Current()
	if s != qaclana.State_DISABLED {
		t.Errorf("unexpected state: %s", s)
	}

	if err == nil {
		t.Errorf("expected error, but didn't fail!")
	}

	err = d.Store(context.Background(), qaclana.State_ENFORCING)
	if err == nil {
		t.Errorf("expected error, but didn't fail!")
	}
}

func db(t *testing.T) (func(), *url.URL) {
	flag.Set("cockroach-binary", "cockroach")
	ts, err := testserver.NewTestServer()
	if err != nil {
		t.Fatal(err)
	}
	if err := ts.Start(); err != nil {
		t.Fatal(err)
	}
	return ts.Stop, ts.PGURL()
}

func schema(t *testing.T, db *sql.DB, s string) {
	q := fmt.Sprintf(`
	CREATE DATABASE IF NOT EXISTS %s;

	CREATE TABLE IF NOT EXISTS %s.kv (
		key varchar(100) primary key,
		value varchar(100) null
	);`, s, s)

	_, err := db.Exec(q)
	if err != nil {
		t.Fatal("failed to create schema", err)
	}
}
