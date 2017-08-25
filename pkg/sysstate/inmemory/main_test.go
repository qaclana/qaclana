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
package inmemory

import (
	"log"
	"testing"

	"gitlab.com/qaclana/qaclana/pkg/proto"
	"gitlab.com/qaclana/qaclana/pkg/sysstate/testutils"
)

func TestWithInMemory(t *testing.T) {
	t.Run("SimpleUpdate", func(t *testing.T) {
		d := WithState(qaclana.State_DISABLED)
		testutils.SimpleUpdate(t, d)
	})
	t.Run("StoreCallsNotifiers", func(t *testing.T) {
		d := WithState(qaclana.State_DISABLED)
		testutils.StoreCallsNotifiers(t, d)
	})
	t.Run("MultipleChanges", func(t *testing.T) {
		d := WithState(qaclana.State_DISABLED)
		log.Println("Starting multiple changes test")
		testutils.MultipleChanges(t, d)
	})
}
