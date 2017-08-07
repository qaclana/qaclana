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

package proxy

import (
	"github.com/spf13/cobra"
)

// NewProxyCommand creates a new sub command grouping all the options for the firewall server component
func NewProxyCommand() *cobra.Command {
	base := &cobra.Command{
		Use:   "proxy",
		Short: "Commands related to the Proxy parts",
		Long: `Manages the Proxy parts of a Qaclana Web Application Firewall cluster.
		
A Proxy is a Firewall Instance and sits between the target service and its client, filtering
the incoming requests and responses. It makes a connection to a firewall server to get the most
updated information, like: system state (enabled, permissive, disabled), white lists, black lists
and so on.
		`,
	}

	base.AddCommand(NewStartProxyCommand())

	return base
}
