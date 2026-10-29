# SoulPass API – Guia para o Front-End

API REST em Java (Spring Boot) que o Front consome.

- **URL base (por enquanto):** `http://localhost:8080`. Depois do deploy, troca para a URL do Render; deixem isso numa constante, ex. `const API_URL = "..."`.
- **Formato:** tudo em JSON. Mandem o header `Content-Type: application/json` no POST e no PUT.
- **Datas** vêm como texto `"2026-10-28"` (AAAA-MM-DD).
- **CORS** já está liberado, então o React pode chamar a API direto com `fetch`.

## Erros

Todo erro vem neste formato. Mostrem o campo `mensagem` na tela:

```json
{ "mensagem": "Saldo insuficiente! Saldo atual: 30.0 | custo do voucher: 100.0 pontos." }
```

| Status | Significado | O que fazer no Front |
|---|---|---|
| 200 | OK | mostrar os dados |
| 201 | Criado | mostrar sucesso / ir para a página do item |
| 204 | Apagado (sem corpo) | remover da lista |
| 400 | Dado inválido | mostrar `mensagem` perto do formulário |
| 404 | Não encontrado | página/aviso de "não encontrado" |
| 409 | Não permitido agora (ex.: saldo insuficiente, já aprovado) | mostrar `mensagem` |
| 500 | Erro no servidor/banco | "Tente novamente mais tarde" |

---

## Tipos TypeScript

Podem copiar direto para `src/types/`.

```ts
export interface Nivel {
  idNivel: number;
  nomeNivel: string;
  nivelOrdem: number;
  pontosXpNecessarios: number;
  descricao: string;
}

export interface Usuario {
  idUsuario: number;
  nome: string;
  cpf: string;
  email: string;
  saldoPontos: number;   // pontos que podem ser gastos (diminui no resgate)
  pontosXp: number;      // total historico (nunca diminui, define o nivel)
  nivel: Nivel;
  dataCadastro: string;
  ativo: boolean;
}

export type TipoMissao =
  | "TRANSPORTE_PUBLICO" | "CAMINHADA" | "BICICLETA" | "DESAFIO_SEMANAL" | "DESAFIO_COLETIVO";

export interface Missao {
  idMissao: number;
  tituloMissao: string;
  tipoMissao: TipoMissao;
  descricao: string;
  pontoBase: number;
  ativa: boolean;
}

export type MetodoValidacao = "CHECKIN" | "QRCODE" | "CODIGO";
export type StatusAcao = "PENDENTE" | "APROVADO" | "REJEITADO";

export interface AcaoRealizada {
  idAcao: number;
  idUsuario: number;
  idMissao: number;
  metodoValidacao: MetodoValidacao;
  codigoValidacao: string;
  dataConclusao: string;
  statusAprovacao: StatusAcao;
  pontosConquistados: number;
}

export type TipoTransporte = "ONIBUS" | "METRO" | "TREM" | "BICICLETA" | "APLICATIVO";

export interface Voucher {
  idVoucher: number;
  tipoTransporte: TipoTransporte;
  custoPontos: number;
  valorMonetario: number;
  descricao: string;
}

export type StatusVoucher = "DISPONIVEL" | "UTILIZADO" | "EXPIRADO";

export interface Resgate {
  idResgate: number;
  idUsuario: number;
  idVoucher: number;
  idAcao: number;          // 0 = sem acao ligada
  codigoGerado: string;
  dataResgate: string;
  dataValidade: string;
  statusVoucher: StatusVoucher;
}

export interface ErroApi {
  mensagem: string;
}
```

---

## Usuários – `/usuarios`

| Método | URL | Corpo | Resposta |
|---|---|---|---|
| GET | `/usuarios` | – | 200 `Usuario[]` |
| GET | `/usuarios/{id}` | – | 200 `Usuario` · 404 |
| POST | `/usuarios` | `{ nome, cpf, email }` | 201 `Usuario` · 400 · 409 (CPF/e-mail já existe) |
| PUT | `/usuarios/{id}` | `{ nome, cpf, email, ativo }` | 200 `Usuario` · 400 · 404 · 409 |
| DELETE | `/usuarios/{id}` | – | 204 · 404 · 409 (tem ações/resgates ligados) |

- No cadastro, a API define sozinha: id, 0 pontos, nível 1 (Iniciante) e a data.
- O PUT **não altera pontos nem nível**. Eles só mudam aprovando ações ou resgatando vouchers.
- Validações: nome com pelo menos 3 letras, CPF com 11 dígitos (com ou sem máscara) e e-mail válido.

```json
POST /usuarios
{ "nome": "Maria Silva", "cpf": "123.456.789-00", "email": "maria@email.com" }
```

## Missões – `/missoes`

| Método | URL | Corpo | Resposta |
|---|---|---|---|
| GET | `/missoes` | – | 200 `Missao[]` (todas) |
| GET | `/missoes/ativas` | – | 200 `Missao[]` (para o usuário escolher) |
| GET | `/missoes/tipo/{tipo}` | – | 200 `Missao[]` (ex.: `/missoes/tipo/bicicleta`) |
| GET | `/missoes/{id}` | – | 200 `Missao` · 404 |
| POST | `/missoes` | `Missao` sem id | 201 · 400 |
| PUT | `/missoes/{id}` | `Missao` sem id | 200 · 400 · 404 |
| DELETE | `/missoes/{id}` | – | 204 · 404 · 409 |

```json
POST /missoes
{ "tituloMissao": "Ir de bike ao trabalho", "tipoMissao": "BICICLETA",
  "descricao": "Pedalar ate o trabalho", "pontoBase": 50, "ativa": true }
```

## Ações realizadas – `/acoes`

O usuário registra que fez uma missão. A ação nasce **PENDENTE** e só dá pontos depois de aprovada com o código.

| Método | URL | Corpo | Resposta |
|---|---|---|---|
| GET | `/acoes` | – | 200 `AcaoRealizada[]` |
| GET | `/acoes/pendentes` | – | 200 `AcaoRealizada[]` |
| GET | `/acoes/usuario/{idUsuario}` | – | 200 `AcaoRealizada[]` (histórico, mais recente primeiro) |
| GET | `/acoes/{id}` | – | 200 · 404 |
| POST | `/acoes` | `{ idUsuario, idMissao, metodoValidacao }` | 201 `AcaoRealizada` (PENDENTE, com `codigoValidacao`) · 400 · 409 |
| PUT | `/acoes/{id}/aprovar` | `{ "codigo": "..." }` | 200 `AcaoRealizada` · 400 · 404 · 409 |
| PUT | `/acoes/{id}/rejeitar` | `{ "motivo": "..." }` (opcional) | 200 · 404 · 409 |
| DELETE | `/acoes/{id}` | – | 204 · 404 · 409 (já aprovada) |

**Fluxo na tela:**
1. `POST /acoes`: a resposta traz o `codigoValidacao`. Mostrem como QR Code ou texto.
2. `PUT /acoes/{id}/aprovar` com esse código. Olhem o `statusAprovacao` da resposta:
   - `APROVADO`: os `pontosConquistados` já foram somados ao usuário (recarreguem `/usuarios/{id}`).
   - `REJEITADO`: o código estava errado e nenhum ponto foi dado.
3. Tentar aprovar de novo devolve **409**, então não dá para ganhar pontos duas vezes.

```json
POST /acoes
{ "idUsuario": 1, "idMissao": 2, "metodoValidacao": "QRCODE" }

PUT /acoes/7/aprovar
{ "codigo": "QRCODE-2-483920" }
```

## Vouchers (catálogo) – `/vouchers`

A "loja": os benefícios que podem ser trocados por pontos. A lista vem do mais barato para o mais caro.

| Método | URL | Corpo | Resposta |
|---|---|---|---|
| GET | `/vouchers` | – | 200 `Voucher[]` |
| GET | `/vouchers/{id}` | – | 200 · 404 |
| POST | `/vouchers` | `Voucher` sem id | 201 · 400 |
| PUT | `/vouchers/{id}` | `Voucher` sem id | 200 · 400 · 404 |
| DELETE | `/vouchers/{id}` | – | 204 · 404 · 409 (já resgatado por alguém) |

```json
POST /vouchers
{ "tipoTransporte": "METRO", "custoPontos": 200, "valorMonetario": 5.40,
  "descricao": "1 passagem de metro" }
```

## Resgates (vouchers do usuário) – `/resgates`

| Método | URL | Corpo | Resposta |
|---|---|---|---|
| GET | `/resgates` | – | 200 `Resgate[]` |
| GET | `/resgates/usuario/{idUsuario}` | – | 200 `Resgate[]` ("Meus vouchers") |
| GET | `/resgates/codigo/{codigo}` | – | 200 · 404 |
| GET | `/resgates/{id}` | – | 200 · 404 |
| POST | `/resgates` | `{ idUsuario, idVoucher }` | 201 `Resgate` · 400 · 409 (saldo insuficiente) |
| PUT | `/resgates/codigo/{codigo}/utilizar` | – | 200 `Resgate` (UTILIZADO) · 404 · 409 (já usado/vencido) |
| PUT | `/resgates/expirar` | – | 200 `{ mensagem }` (rotina de manutenção) |
| DELETE | `/resgates/{id}` | – | 204 (cancela e **devolve os pontos**) · 404 · 409 |

- O resgate debita só o `saldoPontos`. O `pontosXp` e o nível não mudam.
- O voucher vale 90 dias (`dataValidade`).
- Só dá para cancelar voucher `DISPONIVEL`.

```json
POST /resgates
{ "idUsuario": 1, "idVoucher": 3 }
```

---

## Exemplo de service no React (sem Axios: a disciplina proíbe)

```ts
// src/services/missaoService.ts
import type { Missao, ErroApi } from "../types";

const API_URL = "http://localhost:8080";

export async function listarMissoesAtivas(): Promise<Missao[]> {
  const resposta = await fetch(`${API_URL}/missoes/ativas`);
  if (!resposta.ok) {
    const erro: ErroApi = await resposta.json();
    throw new Error(erro.mensagem);
  }
  return resposta.json();
}
```
