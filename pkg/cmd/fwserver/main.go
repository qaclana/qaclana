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

package fwserver

import (
	"github.com/spf13/cobra"
)

// NewFirewallServerCommand creates a new sub command grouping all the options for the firewall server component
func NewFirewallServerCommand() *cobra.Command {
	base := &cobra.Command{
		Use:   "fwserver",
		Short: "Commands related to the Firewall Server parts",
		Long: `Manages the Firewall Server parts of a Qaclana Web Application Firewall cluster.
		
A Firewall Server is the component to which the Firewall Instances connect to. This should be
started after a backend is available, but this is not required: a connection will be started
later if a backend isn't available.
		`,
	}

	base.AddCommand(NewStartFirewallServerCommand())

	return base
}
