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

package processor

// Outcome represents the possible outcomes for a request processor
type Outcome int

const (
	// ALLOW means that the request is known to be OK
	ALLOW Outcome = iota

	// BLOCK means that the request is known to be NOK
	BLOCK

	// NEUTRAL means that the processor can't know for sure whether the request is good
	NEUTRAL
)

func (o Outcome) String() string {
	switch o {
	case ALLOW:
		return "Allow"
	case BLOCK:
		return "Block"
	case NEUTRAL:
		return "Neutral"
	}

	return "Unknown"
}
