-- Custom OIDC helper for OpenResty with proper port forwarding support
-- Source: https://github.com/petereijgermans11/oidc-nginx-bff-websockets

local http  = require "resty.http"
local cjson = require "cjson"

local _M = {}

local function generate_random_string(length)
    local charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    local random_string = {}
    local charset_length = #charset
    for i = 1, length do
        local rand_index = math.random(1, length)
        table.insert(random_string, charset:sub(rand_index, rand_index))
    end
    return table.concat(random_string)
end

function _M.build_authorization_url(opts)
    local state = generate_random_string(32)
    local nonce = generate_random_string(32)

    ngx.ctx.oidc_state = state
    ngx.ctx.oidc_nonce = nonce

    local params = {
        client_id     = opts.client_id,
        redirect_uri  = opts.redirect_uri,
        response_type = "code",
        scope         = opts.scope or "openid profile email",
        state         = state,
        nonce         = nonce
    }

    local query_string = {}
    for k, v in pairs(params) do
        table.insert(query_string, string.format("%s=%s",
            ngx.escape_uri(k),
            ngx.escape_uri(v)))
    end

    return opts.authorization_endpoint .. "?" .. table.concat(query_string, "&")
end

function _M.exchange_code_for_tokens(opts, code)
    local httpc = http.new()
    httpc:set_timeout(10000)

    local body = {
        grant_type    = "authorization_code",
        code          = code,
        redirect_uri  = opts.redirect_uri,
        client_id     = opts.client_id,
        client_secret = opts.client_secret
    }

    local body_string = {}
    for k, v in pairs(body) do
        table.insert(body_string, string.format("%s=%s",
            ngx.escape_uri(k),
            ngx.escape_uri(v)))
    end

    local res, err = httpc:request_uri(opts.token_endpoint, {
        method  = "POST",
        body    = table.concat(body_string, "&"),
        headers = { ["Content-Type"] = "application/x-www-form-urlencoded" }
    })

    if not res then
        return nil, "Failed to connect to token endpoint: " .. (err or "unknown error")
    end
    if res.status ~= 200 then
        return nil, "Token endpoint returned status " .. res.status .. ": " .. (res.body or "")
    end

    return cjson.decode(res.body), nil
end

local sessions = ngx.shared.sessions

local function generate_session_id()
    local random_string = generate_random_string(32)
    return ngx.md5(random_string .. tostring(ngx.now()) .. tostring(ngx.worker.pid()))
end

function _M.get_session()
    local session_id = ngx.var.cookie_session
    if not session_id then return nil end

    local session_json = sessions:get(session_id)
    if not session_json then return nil end

    local ok, session = pcall(cjson.decode, session_json)
    if not ok then return nil end

    return session
end

function _M.set_session(data, max_age)
    local session_id   = generate_session_id()
    local session_json = cjson.encode(data)
    local ttl          = max_age or 3600

    local success, err = sessions:set(session_id, session_json, ttl)
    if not success then
        ngx.log(ngx.ERR, "Failed to store session: ", err or "unknown")
        return nil
    end

    local cookie_parts = {
        "session=" .. session_id,
        "Path=/",
        "HttpOnly",
        "SameSite=Lax"
    }
    if max_age then
        table.insert(cookie_parts, "Max-Age=" .. max_age)
    end

    return table.concat(cookie_parts, "; ")
end

function _M.clear_session()
    local session_id = ngx.var.cookie_session
    if session_id then
        sessions:delete(session_id)
    end
    return "session=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0"
end

return _M
