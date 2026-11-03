# Deploy em servidor (VPS) com Docker Compose

Sobe tudo junto: MySQL, Redis, backend e front. O container `web` (Caddy) serve o site,
encaminha `/api` e `/ws-chat` para o backend e emite o certificado HTTPS sozinho.
So as portas 80 e 443 ficam abertas; banco e Redis nao saem da rede interna.

## O que precisa ter

- Um servidor Linux (Ubuntu) com Docker e o plugin Compose. Recomendado 2 GB de RAM ou mais
  (o build do Java e o MySQL pesam).
- Um dominio ou subdominio com registro A apontando para o IP do servidor.
  Sem dominio nao ha HTTPS, e o login e o chat precisam de HTTPS.
- Portas 80 e 443 liberadas no firewall do servidor e no painel do provedor.

## Primeira subida

```bash
git clone https://github.com/luizhlaguiar/portal-monitoria-unifor.git
cd portal-monitoria-unifor
cp .env.prod.example .env
nano .env
```

Preencha o `.env`. Para gerar senhas e o segredo do JWT:

```bash
openssl rand -hex 24      # senhas
openssl rand -base64 36   # JWT_SECRET
```

Suba:

```bash
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

Abra `https://SEU_DOMINIO`. A primeira subida cria as tabelas (`DDL_AUTO=update`).

## Levar os dados do seu computador

No computador (PowerShell), com o MySQL local rodando:

```powershell
docker exec mysql-monitoria mysqldump -uroot -p portal_monitoria > dump.sql
```

Envie o `dump.sql` ao servidor (scp) e, la:

```bash
docker compose -f docker-compose.prod.yml exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" portal_monitoria' < dump.sql
```

Depois rode, uma vez, no banco do servidor:

```bash
docker compose -f docker-compose.prod.yml exec db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" portal_monitoria -e "ALTER TABLE mensagens MODIFY COLUMN content LONGTEXT;"'
```

Apague o `dump.sql` do servidor e do computador quando terminar.

## Atualizar depois de mudar o codigo

```bash
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

## Backup do banco (faca de tempos em tempos)

```bash
docker compose -f docker-compose.prod.yml exec -T db sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" portal_monitoria' > backup-$(date +%F).sql
```

## Se algo der errado

- Site nao abre com HTTPS: confira se o dominio aponta para o IP certo
  e se as portas 80 e 443 estao abertas. Veja `docker compose -f docker-compose.prod.yml logs web`.
- Backend reinicia sem parar: `logs backend`. Causas comuns: senha do `.env` diferente da usada
  na primeira subida do MySQL (a senha so vale na criacao do volume) ou `JWT_SECRET` curto.
- Chat nao conecta: confira se `DOMAIN` no `.env` e exatamente o endereco aberto no navegador.
- Pouca memoria: aumente a RAM do servidor ou ajuste `JAVA_OPTS` no `.env`.
