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
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	hcServer "gitlab.com/qaclana/qaclana/pkg/healthcheck/server"
	"gitlab.com/qaclana/qaclana/pkg/proxy/server"
)

// NewStartProxyCommand initializes a command that can be used to start the firewall server
func NewStartProxyCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "start",
		Short: "Starts a new proxy server",
		Long:  "Starts a new proxy server",
		Run: func(cmd *cobra.Command, args []string) {
			start(cmd, args)
		},
	}

	cmd.Flags().IntP("port", "p", 8000, "The port to bind the proxy to")
	cmd.Flags().IntP("healthcheck-port", "", 9000, "The port to bind the healthcheck server to")
	cmd.Flags().StringP("server-hostname", "u", "qaclana-server.qaclana-infra.svc.cluster.local", "The hostname to the Qaclana Server")
	cmd.Flags().StringP("target", "t", "http://localhost:80/", "The URL to the target to protect")
	viper.BindPFlag("port", cmd.Flags().Lookup("port"))
	viper.BindPFlag("healthcheck-port", cmd.Flags().Lookup("healthcheck-port"))
	viper.BindPFlag("server-hostname", cmd.Flags().Lookup("server-hostname"))
	viper.BindPFlag("target", cmd.Flags().Lookup("target"))

	return cmd
}

func start(cmd *cobra.Command, args []string) {
	var ch = make(chan os.Signal, 0)
	signal.Notify(ch, os.Interrupt, syscall.SIGTERM)

	hc := hcServer.Start(fmt.Sprintf("0.0.0.0:%d", viper.GetInt("healthcheck-port")))
	defer hc.Close()

	s := server.Start(fmt.Sprintf("0.0.0.0:%d", viper.GetInt("port")), viper.GetString("target"))
	defer s.Close()

	select {
	case <-ch:
		log.Println("Qaclana Proxy Server finished")
	}
}
