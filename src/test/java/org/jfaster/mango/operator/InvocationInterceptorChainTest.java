/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.operator;

import org.jfaster.mango.reflect.Parameter;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.SQLType;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class InvocationInterceptorChainTest {

    @Test
    public void testIntercept() throws Exception {
        final List<Object> args = new ArrayList<Object>();
        args.add(1);
        args.add("ash");
        final String sql = "select * from user where id=? and name=?";
        final User user = new User();
        user.setId(100);
        user.setName("lucy");

        InterceptorChain ic = new InterceptorChain();
        ic.addInterceptor(new Interceptor() {
            @Override
            public void intercept(PreparedSql preparedSql, List<Parameter> parameters, SQLType sqlType) {
                assertThat(preparedSql.getSql(), equalTo(sql));
                assertThat(preparedSql.getArgs(), equalTo(args));
                assertThat((User) parameters.get(0).getValue(), equalTo(user));
                assertThat(sqlType, equalTo(SQLType.SELECT));
            }
        });
        List<Annotation> empty = Collections.emptyList();
        TypeToken<User> t = new TypeToken<User>() {};
        ParameterDescriptor p = new ParameterDescriptor(0, t.getType(), empty, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);
        InvocationInterceptorChain iic = new InvocationInterceptorChain(ic, pds, SQLType.SELECT);

        InvocationContextFactory f = new InvocationContextFactory(new NameProvider(pds));
        InvocationContext ctx = f.newInvocationContext(new Object[] {user});
        PreparedSql ps = new PreparedSql(sql, args);
        iic.intercept(ps, ctx);
    }

}
