{{/*
Create the "controller.quorum.voters" list
*/}}
{{- define "controller.quorum.voters" -}}
{{- $start := $.Values.controller.ordinals.start | int -}}
{{- $end := $.Values.controller.replicas | int -}}
{{- $port := $.Values.controller.port | toString -}}
{{- $controllerList := list -}}
{{- range $id := seq $start $end | splitList " " -}}
{{- $host := printf "controller-%s.controller.%s" $id (include "k8s.subdomain" $) -}}
{{- $controller := printf "%s@%s:%s" $id $host $port -}}
{{- $controllerList = append $controllerList $controller -}}
{{- end -}}
{{- printf "%s" (join "," $controllerList)}}
{{- end -}}

{{/*
Create the "schema.registry.kafkastore.bootstrap.servers" list
*/}}
{{- define "schema.registry.kafkastore.bootstrap.servers" -}}
{{- $start := $.Values.broker.ordinals.start | int -}}
{{- $end := add $start (int $.Values.broker.replicas) -1 | int -}}
{{- $protocol := $.Values.broker.protocol -}}
{{- $port := $.Values.broker.port | toString -}}
{{- $serverList := list -}}
{{- range $id := seq $start $end | splitList " " -}}
{{- $host := printf "broker-%s.broker.%s" $id (include "k8s.subdomain" $) -}}
{{- $bootstrap := printf "%s://%s:%s" $protocol $host $port -}}
{{- $serverList = append $serverList $bootstrap -}}
{{- end -}}
{{- printf "%s" (join "," $serverList)}}
{{- end -}}

{{/*
Create the "connect.bootstrap.servers" list
*/}}
{{- define "connect.bootstrap.servers" -}}
{{- $start := $.Values.broker.ordinals.start | int -}}
{{- $end := add $start (int $.Values.broker.replicas) -1 | int -}}
{{- $port := $.Values.broker.port | toString -}}
{{- $serverList := list -}}
{{- range $id := seq $start $end | splitList " " -}}
{{- $host := printf "broker-%s.broker.%s" $id (include "k8s.subdomain" $) -}}
{{- $bootstrap := printf "%s:%s" $host $port -}}
{{- $serverList = append $serverList $bootstrap -}}
{{- end -}}
{{- printf "%s" (join "," $serverList)}}
{{- end -}}


{{- define "schema.registry.url" -}}
{{- $host := printf "schema-reg.%s" (include "k8s.subdomain" $) -}}
{{- $protocol := $.Values.schema.protocol -}}
{{- $port := $.Values.schema.port | toString -}}
{{- printf "%s://%s:%s" $protocol $host $port -}}
{{- end -}}

{{- define "k8s.subdomain" -}}
{{- printf "%s.svc.%s" $.Release.Namespace $.Values.kubernetes.cluster -}}
{{- end -}}