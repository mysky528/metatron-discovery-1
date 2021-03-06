/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.metatron.discovery.common.oauth.token.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.metatron.discovery.common.oauth.token.cache.CachedToken;
import app.metatron.discovery.common.oauth.token.cache.TokenCacheRepository;
import app.metatron.discovery.util.HttpUtils;

/**
 *
 */
public class WhitelistAuthenticationFilter implements Filter {

  private static Logger LOGGER = LoggerFactory.getLogger(WhitelistAuthenticationFilter.class);

  private TokenCacheRepository tokenCacheRepository;
  private TokenExtractor tokenExtractor = new BearerTokenExtractor();
  private TokenStore jwtTokenStore;

  public void setTokenCacheRepository(TokenCacheRepository tokenCacheRepository) {
    this.tokenCacheRepository = tokenCacheRepository;
  }

  public void setJwtTokenStore(TokenStore jwtTokenStore) {
    this.jwtTokenStore = jwtTokenStore;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    Authentication authentication = tokenExtractor.extract(httpServletRequest);
    // find access token in whitelist cache
    if(authentication != null) {
      String token = authentication.getPrincipal().toString();
      OAuth2Authentication authFromToken = jwtTokenStore.readAuthentication(token);

      // getting username, clientid, clientip
      String username = authFromToken.getName();
      String clientId = authFromToken.getOAuth2Request().getClientId();
      String userHost = HttpUtils.getClientIp(httpServletRequest);
      String userAgent = httpServletRequest.getHeader("user-agent");

      if (tokenCacheRepository.isTimeoutClientDetails(clientId)) {
        // getting whitelist in cache
        CachedToken cachedToken = tokenCacheRepository.getCachedToken(username, clientId);
        if (cachedToken == null) {
          LOGGER.info("Cached Token is not exist({}, {})", username, clientId);
          httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User ip is not in whitelist.");
          return;
        }

        String cachedUserHost = cachedToken.getUserIp();
        // if not matched in whitelist cache, throw exception
        if (!userHost.equals(cachedUserHost)) {
          LOGGER.info("Cached Token's ip ({}) is not matched userIp ({})", cachedUserHost, userHost);
          httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User ip is not in whitelist.");
          return;
        }
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

  }
}
