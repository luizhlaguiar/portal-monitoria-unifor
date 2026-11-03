# Deploy na nuvem: Vercel (front) + Railway (backend, MySQL, Redis)

## 1. Railway (backend)

1. Suba o projeto para o GitHub (o `.env` nao vai: ja esta no .gitignore).
2. No Railway: New Project > Deploy from GitHub repo > escolha o repositorio.
3. No servico criado, em Settings, defina **Root Directory = `backend`**.
   O Railway usa o `backend/Dockerfile` automaticamente.
4. No mesmo projeto: New > Database > MySQL. Depois New > Database > Redis.
5. No servico do backend, em Variables, crie (os nomes entre `${{ }}` dependem
   do nome que o Railway deu aos servicos de banco; confira na aba Variables deles):

   | Variavel        | Valor |
   |-----------------|-------|
   | DB_URL          | jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?allowPublicKeyRetrieval=true |
   | DB_USERNAME     | ${{MySQL.MYSQLUSER}} |
   | DB_PASSWORD     | ${{MySQL.MYSQLPASSWORD}} |
   | REDIS_HOST      | ${{Redis.REDISHOST}} |
   | REDIS_PORT      | ${{Redis.REDISPORT}} |
   | REDIS_PASSWORD  | ${{Redis.REDISPASSWORD}} |
   | JWT_SECRET      | (32+ caracteres aleatorios, fixo) |
   | ALLOWED_ORIGIN  | https://SEU-APP.vercel.app (preencha depois do passo 2) |
   | JAVA_OPTS       | -Xmx512m |

6. Em Settings > Networking, clique em **Generate Domain**. Anote a URL
   (`https://xxxx.up.railway.app`).
7. (Opcional) Levar os dados atuais: importe o dump no MySQL do Railway usando
   os dados de conexao publica dele (`mysql -h ... -P ... -u ... -p < dump.sql`).

## 2. Vercel (front)

1. Add New > Project > importe o repositorio.
2. **Root Directory = `frontend_new`** (o Vercel detecta o Vite sozinho).
3. Environment Variables: `VITE_API_URL=https://URL-DO-RAILWAY/api`
4. Deploy. Anote a URL final.
5. Volte ao Railway e ajuste `ALLOWED_ORIGIN` para essa URL (sem barra no final).
   O backend reinicia sozinho.

## 3. Teste

Abra a URL do Vercel, faca login e teste o chat (WebSocket em `/ws-chat`).

## Problemas comuns

- Erro de CORS ou chat nao conecta: `ALLOWED_ORIGIN` diferente da URL do Vercel.
- Access denied no MySQL: `DB_USERNAME`/`DB_PASSWORD` nao batem com as do servico MySQL.
- NOAUTH/WRONGPASS no Redis: `REDIS_PASSWORD` incorreta.
- Todos deslogam a cada reinicio: faltou fixar `JWT_SECRET`.
- Tela em branco ao recarregar uma rota: confira se `frontend_new/vercel.json` foi commitado.
