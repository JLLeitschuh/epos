/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * icense version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.epos.fes.logical;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.n52.epos.fes.StatementPartial;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public abstract class BiLogicalOperator implements StatementPartial {

    private StatementPartial one;
    private StatementPartial two;
    private final String op;

    public BiLogicalOperator(String op) {
        this(null, null, op);
    }
    
    public BiLogicalOperator(StatementPartial one, StatementPartial two, String op) {
        this.one = one;
        this.two = two;
        this.op = op;
    }

    public void setOne(StatementPartial one) {
        this.one = one;
    }

    public void setTwo(StatementPartial two) {
        this.two = two;
    }

    @Override
    public String getStatementPartial() {
        Objects.requireNonNull(one);
        Objects.requireNonNull(two);
        return String.format("(%s) %s (%s)", one.getStatementPartial(), op, two.getStatementPartial());
    }
    
    public static StatementPartial wrapInAnd(List<StatementPartial> partials) {
        return wrapIn(() -> {return new AndOperator();}, partials);
    }
    
    public static StatementPartial wrapInOr(List<StatementPartial> partials) {
        return wrapIn(() -> {return new OrOperator();}, partials);
    }
    
    private static StatementPartial wrapIn(Supplier<BiLogicalOperator> supplier, List<StatementPartial> partials) {
        BiLogicalOperator current = supplier.get();
        BiLogicalOperator root = current;
        BiLogicalOperator next;
        for (int i = 0; i < partials.size(); i++) {
            StatementPartial p = partials.get(i);
            
            if (i == partials.size() - 2) {
                //second last and last
                next = new AndOperator();
                next.setOne(p);
                next.setTwo(partials.get(i+1));
                current.setTwo(next);
                break;
            }
            else {
                if (i == 0) {
                    //first
                    current.setOne(p);
                }
                else {
                    next = new AndOperator();
                    next.setOne(p);
                    current.setTwo(next);
                    current = next;
                }
            }
            
        }
        
        return root;
    }
    
}
