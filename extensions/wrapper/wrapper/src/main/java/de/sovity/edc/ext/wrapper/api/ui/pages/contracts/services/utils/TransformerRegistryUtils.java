/*
 *  Copyright (c) 2023 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package de.sovity.edc.ext.wrapper.api.ui.pages.contracts.services.utils;

import lombok.RequiredArgsConstructor;
import org.eclipse.edc.api.transformer.DtoTransformerRegistry;
import org.eclipse.edc.web.spi.exception.InvalidRequestException;

@RequiredArgsConstructor
public class TransformerRegistryUtils {

    private final DtoTransformerRegistry transformerRegistry;

    public <T, R> R transformOrThrow(T input, Class<R> outputClazz) {
        var result = transformerRegistry.transform(input, outputClazz);
        if (result.failed()) {
            throw new InvalidRequestException(result.getFailureMessages());
        }

        return result.getContent();
    }
}
