#!/usr/bin/env python3
"""
Script de notificacao de pipeline — SafeRoute CI/CD.

Todas as configuracoes sao lidas de variaveis de ambiente.
Nenhum valor esta fixado (hardcoded) neste script.

Variaveis obrigatorias (configurar no Jenkins):
  SMTP_HOST      - Servidor SMTP  (ex: smtp.gmail.com)
  SMTP_PORT      - Porta SMTP     (padrao: 587)
  SMTP_USER      - Remetente      (ex: ci@seuemail.com)
  SMTP_PASSWORD  - Senha SMTP / App Password
  NOTIFY_EMAIL   - Destinatario   (ex: equipe@seuemail.com)

Variaveis injetadas automaticamente pelo Jenkins:
  BUILD_STATUS   - SUCCESS | FAILURE | UNSTABLE
  BUILD_NUMBER   - Numero do build
  BUILD_URL      - URL do build no Jenkins
  JOB_NAME       - Nome do job
"""

import smtplib
import os
import sys
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart


def get_required_env(name: str) -> str:
    """Le variavel de ambiente obrigatoria. Encerra com erro se ausente."""
    value = os.environ.get(name, "").strip()
    if not value:
        print(f"[notify] ERRO: variavel '{name}' nao definida ou vazia.", file=sys.stderr)
        sys.exit(1)
    return value


# Configuracao SMTP — 100% via variaveis de ambiente
smtp_host     = get_required_env("SMTP_HOST")
smtp_port     = int(os.environ.get("SMTP_PORT", "587"))
smtp_user     = get_required_env("SMTP_USER")
smtp_password = get_required_env("SMTP_PASSWORD")
notify_email  = get_required_env("NOTIFY_EMAIL")

# Informacoes do build — injetadas pelo Jenkins
build_status  = os.environ.get("BUILD_STATUS",  "UNKNOWN")
build_number  = os.environ.get("BUILD_NUMBER",  "?")
build_url     = os.environ.get("BUILD_URL",     "N/A")
job_name      = os.environ.get("JOB_NAME",      "SafeRoute")

# Icone de status
icon = "✅" if build_status == "SUCCESS" else "❌"

subject = f"[SafeRoute CI] {icon} Build #{build_number} — {build_status}"

body = f"""\
SafeRoute — Pipeline Notification
======================================

Job      : {job_name}
Build    : #{build_number}
Status   : {icon} {build_status}
URL      : {build_url}

Artefatos (JARs e relatorios JaCoCo) disponiveis no link acima.

--
SafeRoute CI/CD · Jenkins · Inatel S07
"""

# Monta o e-mail
msg             = MIMEMultipart()
msg["From"]     = smtp_user
msg["To"]       = notify_email
msg["Subject"]  = subject
msg.attach(MIMEText(body, "plain", "utf-8"))

# Envia via SMTP com TLS
try:
    with smtplib.SMTP(smtp_host, smtp_port, timeout=10) as server:
        server.ehlo()
        server.starttls()
        server.login(smtp_user, smtp_password)
        server.send_message(msg)
    print(f"[notify] E-mail enviado para {notify_email}  "
          f"(build #{build_number} — {build_status})")
except Exception as exc:
    print(f"[notify] Falha ao enviar e-mail: {exc}", file=sys.stderr)
    sys.exit(1)
