# P1-T4 · Spring Security + JWT 鉴权体系

| 字段 | 内容 |
|------|------|
| 阶段 | 阶段一 · 项目基础搭建 |
| 周期 | Week 1 – 2 |
| 预估工时 | 3 天 |
| 负责范围 | 后端 |
| 前置依赖 | P1-T2、P1-T3 |

---

## 任务目标

⚠️ **本任务是整个项目最重要的基础。** 实现基于 JWT 的无状态鉴权体系，包括登录、登出、Token 黑名单、接口权限控制。后续所有后台接口均依赖此机制。

---

## 详细步骤

### 1. JwtUtil 工具类

```java
@Component
public class JwtUtil {

    @Value("${voidpen.jwt.secret}")
    private String secret;

    @Value("${voidpen.jwt.expire}")
    private long expireSeconds;

    // 生成 Token
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
            .signWith(getKey())
            .compact();
    }

    // 解析 Token（失败抛出 JwtException）
    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // 获取 Token 剩余有效期（秒）
    public long getRemainingSeconds(String token) {
        Date expiration = parseToken(token).getExpiration();
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

### 2. JWT 认证过滤器

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                // 检查黑名单
                Boolean inBlacklist = redisTemplate.hasKey(RedisKeys.TOKEN_BLACKLIST + token);
                if (Boolean.TRUE.equals(inBlacklist)) {
                    sendUnauthorized(response, "Token 已失效，请重新登录");
                    return;
                }

                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.valueOf(claims.getSubject());
                String role = claims.get("role", String.class);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId, null,
                        List.of(new SimpleGrantedAuthority(role))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException e) {
                sendUnauthorized(response, "Token 无效或已过期");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message)
        );
    }
}
```

### 3. SecurityConfig

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/v1/**").permitAll()
                .requestMatchers("/admin/v1/auth/login").permitAll()
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**",
                                 "/webjars/**", "/swagger-resources/**").permitAll()
                // 后台接口需要 ADMIN 角色
                .requestMatchers("/admin/v1/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"code\":403,\"message\":\"无权限访问\",\"data\":null}");
                })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4. 认证相关接口

```java
@RestController
@RequestMapping("/admin/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return Result.success();
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> info() {
        Long userId = getCurrentUserId();
        return Result.success(authService.getUserInfo(userId));
    }
}
```

```java
// AuthServiceImpl 登录逻辑
public LoginResponse login(LoginRequest request) {
    TUser user = userMapper.selectOne(
        new LambdaQueryWrapper<TUser>()
            .eq(TUser::getUsername, request.getUsername())
    );

    if (user == null) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }
    if (user.getStatus() == 0) {
        throw new BusinessException(ErrorCode.USER_DISABLED);
    }

    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

    // 更新最后登录时间
    userMapper.updateById(new TUser().setId(user.getId()).setLastLoginTime(LocalDateTime.now()));

    return new LoginResponse(token, UserInfoVO.from(user));
}

// 登出逻辑
public void logout(String token) {
    if (StringUtils.hasText(token)) {
        long ttl = jwtUtil.getRemainingSeconds(token);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                RedisKeys.TOKEN_BLACKLIST + token, "1",
                ttl, TimeUnit.SECONDS
            );
        }
    }
}
```

---

## 验收标准

- [ ] `POST /admin/v1/auth/login` 用正确账密返回 `{ token, userInfo }`，密码错误返回 `{ code: 1003 }`
- [ ] 携带有效 Token 访问 `GET /admin/v1/auth/info` 返回当前用户信息
- [ ] 不携带 Token 访问 `/admin/v1/**` 返回 `{ code: 401 }`
- [ ] 登出后原 Token 加入 Redis 黑名单，再次使用该 Token 请求返回 `{ code: 401, message: "Token 已失效" }`
- [ ] 访问 `/api/v1/` 下公开接口无需 Token
- [ ] JWT Token 格式正确（三段 Base64 以 `.` 分隔），可在 [jwt.io](https://jwt.io) 解码验证
- [ ] Token 过期后访问返回 401（可将 expire 临时设为 5s 验证）
