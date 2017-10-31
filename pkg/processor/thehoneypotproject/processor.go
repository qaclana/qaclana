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

// Package thehoneypotproject specifies a processor that matches the client IP with thehoneypotproject
package thehoneypotproject

import (
	"fmt"
	"log"
	"net"
	"net/http"
	"strings"

	opentracing "github.com/opentracing/opentracing-go"
	"github.com/spf13/viper"

	"gitlab.com/qaclana/qaclana/pkg/processor"
)

var p = &pr{}

// pr is the thehoneypotproject's processor implementation
type pr struct {
}

// Process an incoming request, checking it's remote address against the honeypot project's list
func (p *pr) Process(req *http.Request) (processor.Outcome, error) {
	sp, _ := opentracing.StartSpanFromContext(req.Context(), "processor-thehoneypotproject")
	defer sp.Finish()

	ip, _, err := net.SplitHostPort(req.RemoteAddr)
	if err != nil {
		log.Printf("thehoneypotproject: %s", err)
		// let's try to parse it as if it's a IP-only string:
		ip = req.RemoteAddr
	}

	client := net.ParseIP(ip)
	if client == nil {
		return processor.NEUTRAL, fmt.Errorf("thehoneypotproject: %s could not be parsed into an IP", ip)
	}

	apiKey := viper.GetString("thehoneypot-apikey")
	if apiKey == "" {
		return processor.NEUTRAL, fmt.Errorf("thehoneypotproject is not configured, skipping")
	}

	address := fmt.Sprintf("%s.%s.dnsbl.httpbl.org", apiKey, reversedIP(client.String()))
	log.Printf("Looking up %s", address)

	ips, err := net.LookupIP(address)
	if err != nil {
		// should have returned a "no such host" (NXDOMAIN), but there's no cheap way to check it, it seems!
		return processor.NEUTRAL, nil
	}

	// we got results, and this is a bad thing: http://www.projecthoneypot.org/httpbl_api.php
	return processor.BLOCK, fmt.Errorf("thehoneypotproject: the remote address %s yields %s", client.String(), ips[0].String())
}

func reversedIP(c string) string {
	splitted := strings.Split(c, ".")
	return fmt.Sprintf("%s.%s.%s.%s", splitted[3], splitted[2], splitted[1], splitted[0])
}

func (p *pr) String() string {
	return "The Honeypot Project"
}
