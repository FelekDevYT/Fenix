package me.felek.fenix;

import me.felek.fenix.exceptions.FenixAccessException;
import me.felek.fenix.exceptions.FenixInvalidVariableTypeException;
import me.felek.fenix.exceptions.FenixStructureDoesNotExistsException;
import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.exceptions.FenixVariableNotDefinedException;
import me.felek.fenix.exceptions.handled.BreakException;
import me.felek.fenix.exceptions.handled.ContinueException;
import me.felek.fenix.exceptions.handled.ReturnException;
import me.felek.fenix.func.RawArg;
import me.felek.fenix.func.FenixFunction;
import me.felek.fenix.struct.Modifier;
import me.felek.fenix.struct.Struct;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;
import me.felek.fenix.type.dot.DotFunctionExecutor;
import me.felek.fenix.type.dot.DotOutput;
import me.felek.fenix.type.impl.*;
import me.felek.fenix.utils.ComparisonUtils;
import me.felek.fenix.utils.LogicalUtils;
import me.felek.fenix.utils.TypeUtils;
import me.felek.fenix.utils.ValueUtils;
import me.felek.fenix.variable.Environment;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

public class FenixVisitorImpl extends FenixBaseVisitor<Value> {
    private Environment env = new Environment();

    @Override
    public Value visitNot(FenixParser.NotContext ctx) {
        return ValueUtils.negative(visit(ctx.expr()));
    }

    @Override
    public Value visitParens(FenixParser.ParensContext ctx) {
        return super.visitParens(ctx);
    }

    @Override
    public Value visitPow(FenixParser.PowContext ctx) {
        return ValueUtils.pow(visit(ctx.expr(0)), visit(ctx.expr(1)));
    }

    @Override
    public Value visitMulDiv(FenixParser.MulDivContext ctx) {
        String op = ctx.op.getText();

        if (op.equals("/")) {
            return ValueUtils.div(visit(ctx.expr(0)), visit(ctx.expr(1)));
        } else if (op.equals("*")) {
            return ValueUtils.mul(visit(ctx.expr(0)), visit(ctx.expr(1)));
        } else {
            return ValueUtils.per(visit(ctx.expr(0)), visit(ctx.expr(1)));
        }
    }

    @Override
    public Value visitAddSub(FenixParser.AddSubContext ctx) {
        String op = ctx.op.getText();

        if (op.equals("+")) {
            return ValueUtils.add(visit(ctx.expr(0)), visit(ctx.expr(1)));
        } else {
            return ValueUtils.sub(visit(ctx.expr(0)), visit(ctx.expr(1)));
        }
    }

    @Override
    public Value visitComparsion(FenixParser.ComparsionContext ctx) {
        String op = ctx.op.getText();
        Value value1 = visit(ctx.expr(0));
        Value value2 = visit(ctx.expr(1));

        if (op.equals(">")) {
            return ComparisonUtils.greater(value1, value2);
        } else if (op.equals(">=")) {
            return ComparisonUtils.greaterEq(value1, value2);
        } else if (op.equals("<")) {
            return ComparisonUtils.lower(value1, value2);
        } else {
            return ComparisonUtils.lowerEq(value1, value2);
        }
    }

    @Override
    public Value visitEquality(FenixParser.EqualityContext ctx) {
        String op = ctx.op.getText();
        Value value1 = visit(ctx.expr(0));
        Value value2 = visit(ctx.expr(1));

        if (op.equals("==")) {
            return ComparisonUtils.eq(value1, value2);
        } else {
            return ComparisonUtils.notEq(value1, value2);
        }
    }

    @Override
    public Value visitAnd(FenixParser.AndContext ctx) {
        return LogicalUtils.and(visit(ctx.expr(0)), visit(ctx.expr(1)));
    }

    @Override
    public Value visitOr(FenixParser.OrContext ctx) {
        return LogicalUtils.or(visit(ctx.expr(0)), visit(ctx.expr(1)));
    }

    @Override
    public Value visitXor(FenixParser.XorContext ctx) {
        return LogicalUtils.xor(visit(ctx.expr(0)), visit(ctx.expr(1)));
    }

    @Override
    public Value visitInt(FenixParser.IntContext ctx) {
        return new IntValue(Integer.parseInt(ctx.INT().getText()));
    }

    @Override
    public Value visitFloat(FenixParser.FloatContext ctx) {
        return new FloatValue(Float.parseFloat(ctx.FLOAT().getText()));
    }

    @Override
    public Value visitString(FenixParser.StringContext ctx) {
        String raw = ctx.STRING().getText();

        String content;
        if (raw.startsWith("\"\"\"")) {
            content = raw.substring(3, raw.length() - 3);
        } else {
            content = raw.substring(1, raw.length() - 1);
        }
        return new StringValue(content);
    }

    @Override
    public Value visitNull(FenixParser.NullContext ctx) {
        return new NullValue();
    }

    @Override
    public Value visitBool(FenixParser.BoolContext ctx) {
        return new BoolValue(Boolean.parseBoolean(ctx.BOOL().getText()));
    }

    @Override
    public Value visitArrayAccess(FenixParser.ArrayAccessContext ctx) {
        String name = ctx.ID().getText();
        Value rawArray = env.get(name);

        if (rawArray == null) {
            throw new FenixVariableNotDefinedException(name);
        }

        Value currentArray = rawArray;
        for (FenixParser.ExprContext exprCtx : ctx.expr()) {
            Value indexValue = visit(exprCtx);
            if (indexValue.getType() != ValueType.INT) {
                throw new FenixTypeException();
            }
            int index = indexValue.asInt();

            if (!(currentArray instanceof ArrayValue)) {
                throw new FenixTypeException();
            }
            ArrayValue array = (ArrayValue) currentArray;
            if (index < 0 || array.getRawArray().size() <= index) {
                throw new RuntimeException("index of bouns exception");//todo: exception
            }
            currentArray = array.get(index);
        }

        return currentArray;
    }

    @Override
    public Value visitCall(FenixParser.CallContext ctx) {
        Environment previousEnv = env;

        if (ctx.ID().getText().equals("println")) {
            System.out.println(visit(ctx.args().expr(0)).asString());
            return new NullValue();
        } else if (ctx.ID().getText().equals("typeof")) {
            return new StringValue(visit(ctx.args().expr(0)).getType().name());
        }
        FenixFunction function = env.getFunction(ctx.ID().getText());
        List<Value> values = new ArrayList<>();
        for (FenixParser.ExprContext arg : ctx.args().expr()) {
            values.add(visit(arg));
        }

        if (values.size() != function.getArgs().size()) {
            throw new RuntimeException();//todo: error
        }

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getType() != function.getArgs().get(i).type()) {
                throw new RuntimeException();//todo: error
            }
        }

        Environment fnEnv = new Environment(env);
        for (int i = 0; i < values.size(); i++) {
            fnEnv.define(function.getArgs().get(i).name(), values.get(i));
        }

        env = fnEnv;
        try {
            visit(function.getBody());
        } catch (ReturnException exc) {
            if (exc.getReturned().getType() != function.getReturnType()) {
                throw new RuntimeException();//todo: error
            }
            return exc.getReturned();
        } finally {
            env = previousEnv;
        }

        return new NullValue();
    }

    @Override
    public Value visitVarDecl(FenixParser.VarDeclContext ctx) {
        if (ctx.varDecl_typed() != null) {
            return visit(ctx.varDecl_typed());
        } else {
            return visit(ctx.varDecl_auto());
        }
    }

    @Override
    public Value visitVarDecl_typed(FenixParser.VarDecl_typedContext ctx) {
        ValueType type = TypeUtils.getTypeFromString(ctx.TYPE().getText().replace("[]", ""));
        String varName = ctx.ID().getText();
        Value value = new NullValue();//todo: type check

        if (!ctx.TYPE().getText().contains("[]")) {
            if (ctx.expr() != null) {
                value = visit(ctx.expr());
            } else {
                value = new IntValue(0);//todo: not good idea
            }

            if (value.getType() != type) {
                throw new FenixInvalidVariableTypeException(varName, value.getType().name());
            }

            env.define(varName, value);

            return value;
        } else {
            int dimensions = 0;
            for (char ch : ctx.TYPE().getText().toCharArray()) {
                if (ch == '[') {
                    dimensions++;
                }
            }

            ArrayValue array = new ArrayValue(new ArrayList<>());
            //todo: ADD TO AUTO
            if (ctx.value == null) {
                return array;
            }

            Value val = visit(ctx.value);
            if (val.getType() == ValueType.ARRAY) {
                array.merge((ArrayValue) val);
            }

            env.define(varName, array);

            return array;
        }
    }

    @Override
    public Value visitVarDecl_auto(FenixParser.VarDecl_autoContext ctx) {
        String name = ctx.ID().getText();
        Value value = visit(ctx.expr());

        env.define(name, value);

        return value;
    }

    @Override
    public Value visitVar(FenixParser.VarContext ctx) {
        return env.get(ctx.ID().getText());
    }

    @Override
    public Value visitIfStatement(FenixParser.IfStatementContext ctx) {
        Value value = visit(ctx.expr(0));
        FenixParser.StatementContext statement = ctx.statement(0);
        FenixParser.StatementContext elseSttmt = ctx.elseSttmt;

        if (value.asBool()) {
            visit(statement);
        } else {
            if (ctx.ELIF_WORD() != null) {
                for (int i = 1; i < ctx.expr().size(); i++) {
                    if (visit(ctx.expr(i)).asBool()) {
                        visit(ctx.statement(i));
                        return new NullValue();
                    }
                }
            }

            visit(elseSttmt);
        }

        return new NullValue();
    }

    @Override
    public Value visitWhileStatement(FenixParser.WhileStatementContext ctx) {
        FenixParser.ExprContext context = ctx.expr();
        FenixParser.StatementContext statement = ctx.statement();

        while (visit(context).asBool()) {
            try {
                visit(statement);
            } catch (BreakException be) {
                break;
            } catch (ContinueException ce) {
                continue;
            }
        }

        return new NullValue();
    }

    @Override
    public Value visitTernary(FenixParser.TernaryContext ctx) {
        FenixParser.ExprContext condition = ctx.expr(0);
        FenixParser.ExprContext expr1 = ctx.expr(1);
        FenixParser.ExprContext expr2 = ctx.expr(2);

        Value value = new NullValue();
        if (visit(condition).asBool()) {
            value = visit(expr1);
        } else {
            value = visit(expr2);
        }

        return value;
    }

    @Override
    public Value visitAssignmentStatement(FenixParser.AssignmentStatementContext ctx) {
        String varName = ctx.ID().getText();
        boolean isArray = ctx.arrayAccessHelper() != null;

        if (isArray) {
            varName = ctx.arrayAccessHelper().arr.getText();
        }

        Value value = visit(ctx.expr());

        if (isArray) {
            List<FenixParser.ExprContext> exprs = ctx.arrayAccessHelper().expr();

            Value rawArray = null;
            if (ctx.SELF_WORD() != null) {
                Value self = env.get("self");
                if (self == null || !(self instanceof SelfValue)) {
                    throw new RuntimeException("Self outside a struct.");
                }

                ((SelfValue) self).getSelf().getVariables().get(varName);
            } else {
                rawArray = env.get(varName);
            }

            if (rawArray == null) {
                throw new FenixVariableNotDefinedException(varName);
            }

            Value currentArray = rawArray;
            for (int i = 0; i < exprs.size()-1; i++) {
                int idx = visit(exprs.get(i)).asInt();
                if (!(currentArray instanceof ArrayValue)) {
                    throw new RuntimeException();//todo: exception
                }
                ArrayValue arr = (ArrayValue) currentArray;
                if (idx < 0 || arr.getRawArray().size() <= idx) {
                    throw new RuntimeException("Index out of bounds");
                }
                currentArray = arr.get(idx);
            }

            int lastIdx = visit(exprs.get(exprs.size() - 1)).asInt();
            if (!(currentArray instanceof ArrayValue)) {
                throw new RuntimeException();//todo: exception
            }
            ArrayValue arr = (ArrayValue) currentArray;
            if (lastIdx < 0 || arr.getRawArray().size() <= lastIdx) {
                throw new RuntimeException("index out of bounds");//todo: excpetion
            }

            arr.getRawArray().set(lastIdx, value);
            return value;
        }

        if (ctx.SELF_WORD() != null) {
            Value self = env.get(varName);
            if (self == null || !(self instanceof SelfValue)) {
                throw new RuntimeException("self outside of a struct context.");
            }

            Struct s = ((SelfValue) self).getSelf();
            s.getVariables().put(varName, value);
            return value;
        }

        env.assign(varName, value);
        return value;
    }

    @Override
    public Value visitSelfFieldAccess(FenixParser.SelfFieldAccessContext ctx) {
        String varName = ctx.ID().getText();

        Value self = env.get("self");
        if (self == null | !(self instanceof SelfValue)) {
            throw new RuntimeException("self outside of a struct.");
        }

        Struct s = ((SelfValue) self).getSelf();
        Value value = s.getVariables().get(varName);

        if (value == null) {
            throw new FenixVariableNotDefinedException(varName);
        }

        return value;
    }

    @Override
    public Value visitForStatement(FenixParser.ForStatementContext ctx) {
        FenixParser.ForInitContext forInit = ctx.forInit();
        FenixParser.ForConditionContext forCondition = ctx.forCondition();
        FenixParser.ForIncrementContext forIncrement = ctx.forIncrement();

        if (forInit != null) {
            visit(forInit);
        }

        while (true) {
            if (forCondition != null) {
                if (!visit(forCondition).asBool()) {
                    break;
                }
            }

            if (ctx.statement() != null) {
                try {
                    visit(ctx.statement());
                } catch (BreakException be) {
                    break;
                } catch (ContinueException ce) {
                    if (forIncrement != null) {
                        visit(forIncrement);
                    }
                    continue;
                }
            }

            if (forIncrement != null) {
                visit(forIncrement);
            }
        }

        return new NullValue();
    }

    @Override
    public Value visitForIncrement(FenixParser.ForIncrementContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Value visitForCondition(FenixParser.ForConditionContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Value visitForInit(FenixParser.ForInitContext ctx) {
        return visit(ctx.varDecl_noSemi());
    }

    @Override
    public Value visitBreakStatement(FenixParser.BreakStatementContext ctx) {
        throw new BreakException();
    }

    @Override
    public Value visitContinueStatement(FenixParser.ContinueStatementContext ctx) {
        throw new ContinueException();
    }

    @Override
    public Value visitFuncDecl(FenixParser.FuncDeclContext ctx) {
        String name = ctx.functionTemplate().ID().getText();
        FenixParser.RawArgsContext rawArgs = ctx.functionTemplate().rawArgs();
        //todo: add return value here please
        FenixParser.StatementContext block = ctx.statement();
        ValueType returnType = ValueType.NULL;
        if (ctx.functionTemplate().TYPE() != null) {
            returnType = TypeUtils.getTypeFromString(ctx.functionTemplate().TYPE().getText());
        }

        List<RawArg> arguments = new ArrayList<>();
        for (var arg : rawArgs.arg()) {
            arguments.add(new RawArg(arg.ID().getText(), TypeUtils.getTypeFromString(arg.TYPE().getText())));
        }

        env.defineFunction(name, new FenixFunction(name, arguments, block, returnType));
        return new NullValue();
    }

    @Override
    public Value visitExprStatement(FenixParser.ExprStatementContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Value visitReturnStatement(FenixParser.ReturnStatementContext ctx) {
        throw new ReturnException(visit(ctx.expr()));
    }

    @Override
    public Value visitBlock(FenixParser.BlockContext ctx) {
        Environment prev = env;
        env = new Environment(prev);

        try {
            for (FenixParser.StatementContext sttmt : ctx.statement()) {
                visit(sttmt);
            }
        } finally {
            env = prev;
        }
        return new NullValue();
    }

    @Override
    public Value visitArray(FenixParser.ArrayContext ctx) {
        ArrayValue array = new ArrayValue(List.of());

        if (ctx.args() != null) {
            for (var v : ctx.args().expr()) {
                array.addValue(visit(v));
            }
        }

        return array;
    }

    @Override
    public Value visitPostfixIncrement(FenixParser.PostfixIncrementContext ctx) {
        Value value = env.get(ctx.ID().getText());
        Value nvalue = ValueUtils.increment(value);
        env.assign(ctx.ID().getText(), value);

        return nvalue;
    }

    @Override
    public Value visitPostfixDecrement(FenixParser.PostfixDecrementContext ctx) {
        Value value = env.get(ctx.ID().getText());
        Value nvalue = ValueUtils.decrement(value);
        env.assign(ctx.ID().getText(), value);

        return nvalue;
    }

    @Override
    public Value visitForEachStatement(FenixParser.ForEachStatementContext ctx) {
        FenixParser.VarDecl_noSemiContext assign = ctx.varDecl_noSemi();
        FenixParser.ExprContext expr = ctx.expr();
        FenixParser.StatementContext sttmt = ctx.statement();
        //for (var a: Int in arr)
        String varName;

        if (assign.varDecl_auto() != null) {
            varName = assign.varDecl_auto().ID().getText();
        } else {
            varName = assign.varDecl_typed().ID().getText();
        }

        if (visit(expr).getType() != ValueType.ARRAY) {
            throw new FenixTypeException();
        }

        ArrayValue array = (ArrayValue) visit(expr);
        if (!array.getRawArray().isEmpty() && array.get(0).getType() != visit(assign).getType()) {
            throw new FenixTypeException();
        }

        for (Value value : array.getRawArray()) {
            env.assign(varName, value);
            try {
                visit(sttmt);
            } catch (BreakException be) {
                break;
            } catch (ContinueException ce) {
                continue;
            }
        }

        return new NullValue();
    }

    @Override
    public Value visitDoWhileStatement(FenixParser.DoWhileStatementContext ctx) {
        FenixParser.StatementContext sttmt = ctx.statement();
        FenixParser.ExprContext condition = ctx.expr();

        do {
            try {
                visit(sttmt);
            } catch (BreakException be) {
                break;
            } catch (ContinueException ce) {
                continue;
            }
        } while (visit(condition).asBool());

        return new NullValue();
    }

    @Override
    public Value visitRange(FenixParser.RangeContext ctx) {
        int v1 = visit(ctx.expr(0)).asInt();
        int v2 = visit(ctx.expr(1)).asInt();
        List<Value> valueArray = new ArrayList<>();
        IntStream.rangeClosed(v1, v2).forEach((el) -> valueArray.add(new IntValue(el)));

        return new ArrayValue(valueArray);
    }

    @Override
    public Value visitStructDeclaration(FenixParser.StructDeclarationContext ctx) {
        String name = ctx.ID().getText();
        if (env.structureExistsInLocalScope(name)) {
            throw new RuntimeException();//todo: exception
        }

        Struct struct = new Struct(name, env);
        List<FenixParser.StructMemberContext> memberContexts = ctx.structMember();
        Environment prevEnv = env;
        Environment tempEnv = new Environment(env);
        env = tempEnv;

        try {
            for (var member : memberContexts) {
                if (member.varDecl() != null) {
                    String varName;
                    if (member.varDecl().varDecl_auto() != null) {
                        varName = member.varDecl().varDecl_auto().ID().getText();
                    } else {
                        varName = member.varDecl().varDecl_typed().ID().getText();
                    }

                    Value value = visit(member.varDecl());

                    struct.getVariables().put(varName, value);
                } else {
                    String functionName = member.functionTemplate().ID().getText();

                    List<RawArg> arguments = new ArrayList<>();
                    for (var arg : member.functionTemplate().rawArgs().arg()) {
                        arguments.add(new RawArg(arg.ID().getText(), TypeUtils.getTypeFromString(arg.TYPE().getText())));
                    }

                    ValueType type = ValueType.NULL;
                    if (member.functionTemplate().TYPE() != null) {
                        type = TypeUtils.getTypeFromString(member.functionTemplate().TYPE().getText());
                    }

                    List<Modifier> modifiers = new ArrayList<>();
                    if (member.MODIFIER() != null) {
                        modifiers = TypeUtils.parseModifiers(member.MODIFIER());
                    }

                    FenixFunction function = new FenixFunction(functionName, arguments, null, type);
                    function.setFunctionModifiers(modifiers);
                    struct.getFunctions().put(functionName, function);
                }
            }
        } finally {
            env = prevEnv;
        }

        env.defineStruct(name, struct);

        return new NullValue();
    }

    @Override
    public Value visitStructFunctionDeclaration(FenixParser.StructFunctionDeclarationContext ctx) {
        String structID = ctx.ID(0).getText();
        String funcID = ctx.ID(1).getText();

        Struct struct = env.getStruct(structID);

        if (struct.getFunctions().containsKey(funcID) && struct.getFunctions().get(funcID).getBody() != null) {
            throw new RuntimeException("Function " + funcID + " already defined in struct " + structID);
        }

        List<RawArg> arguments = new ArrayList<>();
        for (var arg : ctx.rawArgs().arg()) {
            arguments.add(new RawArg(arg.ID().getText(), TypeUtils.getTypeFromString(arg.TYPE().getText())));
        }

        ValueType type = ValueType.NULL;
        if (ctx.TYPE() != null) {
            type = TypeUtils.getTypeFromString(ctx.TYPE().getText());
        }

        List<Modifier> modifiers = struct.getFunctions().get(funcID).getFunctionModifiers();

        FenixFunction func = new FenixFunction(funcID, arguments, ctx.statement(), type);
        func.setFunctionModifiers(modifiers);
        struct.getFunctions().put(funcID, func);

        return new NullValue();
    }

    @Override
    public Value visitStructMemberCallAndObjectFunctionCall(FenixParser.StructMemberCallAndObjectFunctionCallContext ctx) {
        Environment previousEnv = env;
        String SVID = ctx.ID(0).getText();//structure/variable ID
        String funcID = ctx.ID(1).getText();

        if (env.variableExistsInLocalScope(SVID)) {
            //then its: structName.function(a, b, c....);
            List<Value> values = new ArrayList<>();
            for (FenixParser.ExprContext arg : ctx.args().expr()) {
                values.add(visit(arg));
            }

            DotOutput out = DotFunctionExecutor.execute(env.get(SVID), funcID, values);
            if (out.isExecuted()) {
                return out.value();
            }

            if (env.get(SVID).getType() != ValueType.OBJECT) {
                throw new FenixTypeException();
            }

            ObjectValue obj = (ObjectValue) env.get(SVID);
            if (!obj.getObject().getFunctions().containsKey(funcID)) {
                throw new RuntimeException();//todo: fenixfunctionnotfoundexception
            }

            FenixFunction function = obj.getObject().getFunctions().get(funcID);
            if (new HashSet<>(function.getFunctionModifiers()).contains(Modifier.LOC)) {
                throw new FenixAccessException(funcID);
            }

            if (values.size() != function.getArgs().size()) {
                throw new RuntimeException();//todo: error
            }

            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).getType() != function.getArgs().get(i).type()) {
                    throw new RuntimeException();//todo: error
                }
            }

            Environment fnEnv = new Environment(env);
            for (int i = 0; i < values.size(); i++) {
                fnEnv.define(function.getArgs().get(i).name(), values.get(i));
            }
            fnEnv.define("self", new SelfValue(((ObjectValue) env.get(SVID)).getObject()));

            env = fnEnv;
            try {
                visit(function.getBody());
            } catch (ReturnException exc) {
                if (exc.getReturned().getType() != function.getReturnType()) {
                    throw new RuntimeException();//todo: error
                }
                return exc.getReturned();
            } finally {
                env = previousEnv;
            }

            return new NullValue();
        }

        if (!env.structureExistsInLocalScope(SVID) || !env.getStruct(SVID).getFunctions().containsKey(funcID)) {
            throw new RuntimeException();//todo: exception
        }

        FenixFunction function = env.getStruct(SVID).getFunctions().get(funcID);
        if (!new HashSet<>(function.getFunctionModifiers()).containsAll(List.of(Modifier.PUB, Modifier.STATIC))) {
            throw new FenixAccessException(funcID);
        }

        List<Value> values = new ArrayList<>();
        for (FenixParser.ExprContext arg : ctx.args().expr()) {
            values.add(visit(arg));
        }

        if (values.size() != function.getArgs().size()) {
            throw new RuntimeException();//todo: error
        }

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getType() != function.getArgs().get(i).type()) {
                throw new RuntimeException();//todo: error
            }
        }

        Environment fnEnv = new Environment(env);
        for (int i = 0; i < values.size(); i++) {
            fnEnv.define(function.getArgs().get(i).name(), values.get(i));
        }
        fnEnv.define("self", new SelfValue(env.getStruct(SVID)));

        env = fnEnv;
        try {
            visit(function.getBody());
        } catch (ReturnException exc) {
            if (exc.getReturned().getType() != function.getReturnType()) {
                throw new RuntimeException();//todo: error
            }
            return exc.getReturned();
        } finally {
            env = previousEnv;
        }

        return new NullValue();
    }

    @Override
    public Value visitNewExpr(FenixParser.NewExprContext ctx) {
        String structName = ctx.ID().getText();
        if (!env.structureExistsInLocalScope(structName)) {
            throw new FenixStructureDoesNotExistsException(structName);
        }

        Struct s = env.getStruct(structName);
        ObjectValue obj = new ObjectValue(s);

        return obj;
    }

    @Override
    public Value visitBitwiseAnd(FenixParser.BitwiseAndContext ctx) {
        if (visit(ctx.expr(0)).getType() != ValueType.INT || visit(ctx.expr(1)).getType() != ValueType.INT) {
            throw new FenixTypeException();
        }

        return new IntValue(visit(ctx.expr(0)).asInt() & visit(ctx.expr(1)).asInt());
    }

    @Override
    public Value visitBitwiseOr(FenixParser.BitwiseOrContext ctx) {
        if (visit(ctx.expr(0)).getType() != ValueType.INT || visit(ctx.expr(1)).getType() != ValueType.INT) {
            throw new FenixTypeException();
        }

        return new IntValue(visit(ctx.expr(0)).asInt() | visit(ctx.expr(1)).asInt());
    }

    @Override
    public Value visitBitwiseLeft(FenixParser.BitwiseLeftContext ctx) {
        if (visit(ctx.expr(0)).getType() != ValueType.INT || visit(ctx.expr(1)).getType() != ValueType.INT) {
            throw new FenixTypeException();
        }

        return new IntValue(visit(ctx.expr(0)).asInt() << visit(ctx.expr(1)).asInt());
    }

    @Override
    public Value visitBitwiseRight(FenixParser.BitwiseRightContext ctx) {
        if (visit(ctx.expr(0)).getType() != ValueType.INT || visit(ctx.expr(1)).getType() != ValueType.INT) {
            throw new FenixTypeException();
        }

        return new IntValue(visit(ctx.expr(0)).asInt() >> visit(ctx.expr(1)).asInt());
    }

    @Override
    public Value visitIs(FenixParser.IsContext ctx) {
        ValueType type = TypeUtils.getTypeFromString(ctx.TYPE().getText());

        if (type != visit(ctx.expr()).getType()) {
            return new BoolValue(false);
        } else {
            return new BoolValue(true);
        }
    }

    @Override
    public Value visitElvis(FenixParser.ElvisContext ctx) {
        Value v1 = visit(ctx.expr(0));
        Value v2 = visit(ctx.expr(1));

        if (v1.getType() != ValueType.NULL) {
            return v2;
        }

        return new NullValue();
    }
}
