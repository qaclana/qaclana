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
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	"gitlab.com/qaclana/qaclana/pkg/backend/server"
	"gitlab.com/qaclana/qaclana/pkg/backend/sysstate"
	hcServer "gitlab.com/qaclana/qaclana/pkg/healthcheck/server"
)

// NewStartBackendCommand initializes a command that can be used to start the backend server
func NewStartBackendCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "start",
		Short: "Starts a new backend server",
		Long:  "Starts a new backend server",
		Run: func(cmd *cobra.Command, args []string) {
			start(cmd, args)
		},
	}

	cmd.Flags().IntP("port", "p", 8000, "The port to bind the backend to")
	cmd.Flags().IntP("healthcheck-port", "", 9000, "The port to bind the healthcheck server to")
	cmd.Flags().IntP("grpc-port", "", 10000, "The port to bind the gRPC interface to")
	cmd.Flags().StringP("database-url", "", "postgresql://root@cockroachdb-public.qaclana-infra.svc.cluster.local:26257/qaclana?sslmode=disable", "The URL to the database")
	viper.BindPFlag("port", cmd.Flags().Lookup("port"))
	viper.BindPFlag("healthcheck-port", cmd.Flags().Lookup("healthcheck-port"))
	viper.BindPFlag("grpc-port", cmd.Flags().Lookup("grpc-port"))
	viper.BindPFlag("database-url", cmd.Flags().Lookup("database-url"))

	return cmd
}

func start(cmd *cobra.Command, args []string) {
	var ch = make(chan os.Signal, 0)
	signal.Notify(ch, os.Interrupt, syscall.SIGTERM)

	hc := hcServer.Start(fmt.Sprintf("0.0.0.0:%d", viper.GetInt("healthcheck-port")))
	s := server.StartHttpServer(fmt.Sprintf("0.0.0.0:%d", viper.GetInt("port")))
	g, _ := server.StartGrpcServer(fmt.Sprintf("0.0.0.0:%d", viper.GetInt("grpc-port")))
	sysstate.StartBroadcaster()

	select {
	case <-ch:
		log.Println("Qaclana Backend is finishing")
		s.Close()
		hc.Close()
		g.Stop()
		sysstate.Stop()
		log.Println("Qaclana Backend finished")
	}
}
