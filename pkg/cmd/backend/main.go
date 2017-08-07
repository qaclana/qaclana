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

package backend

import (
	"github.com/spf13/cobra"
)

// NewBackendCommand creates a new sub command grouping all the options for the backend component
func NewBackendCommand() *cobra.Command {
	base := &cobra.Command{
		Use:   "backend",
		Short: "Commands related to the Backend parts",
		Long:  "Manages the backend parts of a Qaclana Web Application Firewall cluster",
	}

	base.AddCommand(NewStartBackendCommand())

	return base
}
