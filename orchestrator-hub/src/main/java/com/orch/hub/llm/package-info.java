/**
 * LLM provider wiring (AgentScope {@code Model} bean) and endpoint
 * validation. Hides the third-party model SDK behind a Spring bean
 * surface configured by {@code orch.llm.*} properties.
 *
 * <p>Package-level {@link org.jspecify.annotations.NullMarked} — all
 * unannotated types in this package are non-null by default.</p>
 */
@NullMarked
package com.orch.hub.llm;

import org.jspecify.annotations.NullMarked;
