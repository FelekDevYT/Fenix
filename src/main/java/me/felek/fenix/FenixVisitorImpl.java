package me.felek.fenix;

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

import java.util.ArrayList;
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
        return visit(ctx.expr());
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
        if (ctx.INT().getText().startsWith("0x")) {
            return new IntValue(Integer.parseInt(ctx.INT().getText().substring(2), 16));
        } else if (ctx.INT().getText().startsWith("0o")) {
            return new IntValue(Integer.parseInt(ctx.INT().getText().substring(2), 8));
        } else if (ctx.INT().getText().startsWith("0b")) {
            return new IntValue(Integer.parseInt(ctx.INT().getText().substring(2), 2));
        } else if (ctx.INT().getText().contains("_")) {
            return new IntValue(Integer.parseInt(ctx.INT().getText().replaceAll("_", "")));
        }
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
    public Value visitCall(FenixParser.CallContext ctx) {
        Environment previousEnv = env;

        String funcName = null;
        if (ctx.expr() instanceof FenixParser.VarContext) {
            funcName = ((FenixParser.VarContext) ctx.expr()).ID().getText();
        }

        List<Value> args = new ArrayList<>();
        if (ctx.args() != null) {
            for (FenixParser.ExprContext arg : ctx.args().expr()) {
                args.add(visit(arg));
            }
        }

        if ("println".equals(funcName)) {
            System.out.println(args.get(0).asString());
            return new NullValue();
        } else if ("typeof".equals(funcName)) {
            return new StringValue(args.get(0).getType().name());
        }

        FenixFunction function = null;
        if (funcName != null) {
            try {
                function = env.getFunction(funcName);
            } catch (Exception e) {
                //function not found
                //try to use as expr;
            }
        }

        if (function == null && funcName == null) {
            throw new RuntimeException("Cant call not-func expression");//todo:exception
        }

        if (function == null) {
            throw new RuntimeException("Function not found");
        }

        if (args.size() != function.getArgs().size()) {
            throw new RuntimeException("invalid count of args");
        }

        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).getType() != function.getArgs().get(i).type()) {
                throw new FenixTypeException();
            }
        }

        Environment fnEnv = new Environment(env);
        for (int i = 0; i < args.size(); i++) {
            fnEnv.define(function.getArgs().get(i).name(), args.get(i));
        }

        env = fnEnv;
        try {
            visit(function.getBody());
        } catch (ReturnException exc) {
            if (exc.getReturned().getType() != function.getReturnType()) {
                throw new FenixTypeException();
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

            if (value.getType() != ValueType.NULL && value.getType() != type) {
                throw new FenixInvalidVariableTypeException(varName, value.getType().name());
            }

            env.define(varName, value);

            return value;
        } else {
            if (ctx.value != null) {
                Value val = visit(ctx.value);
                if (val.getType() == ValueType.ARRAY) {
                    env.define(varName, val);
                    return val;
                } else {
                    throw new FenixInvalidVariableTypeException(varName, val.getType().name());
                }
            } else {
                ArrayValue array = new ArrayValue(new ArrayList<>());
                env.define(varName, array);
                return array;
            }
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

            if (elseSttmt != null) {
                visit(elseSttmt);
            }
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
        FenixParser.ExprContext left = ctx.expr(0);
        Value right = visit(ctx.expr(1));

        if (left instanceof FenixParser.VarContext) {
            String varName = ((FenixParser.VarContext) left).ID().getText();
            env.assign(varName, right);
            return right;
        }

        if (left instanceof FenixParser.FieldAccessContext) {
            FenixParser.FieldAccessContext fieldAccessContext = (FenixParser.FieldAccessContext) left;
            Value value = visit(fieldAccessContext.expr());
            String fieldName = fieldAccessContext.ID().getText();

            if (value instanceof ObjectValue) {
                ((ObjectValue) value).getObject().getVariables().put(fieldName, right);
                return right;
            }

            if (value instanceof SelfValue) {
                ((SelfValue) value).getSelf().getVariables().put(fieldName, right);
                return right;
            }

            throw new FenixTypeException();
        }

        if (left instanceof FenixParser.IndexAccessContext) {
            FenixParser.IndexAccessContext indexAccess = (FenixParser.IndexAccessContext) left;
            Value array = visit(indexAccess.expr(0));
            Value indexValue = visit(indexAccess.expr(1));

            if (indexValue.getType() != ValueType.INT) {
                throw new FenixTypeException();
            }
            int idx = indexValue.asInt();

            if (!(array instanceof ArrayValue)) {
                throw new FenixTypeException();
            }

            ArrayValue arr = (ArrayValue) array;
            if (idx < 0 || idx >= arr.getRawArray().size()) {
                throw new RuntimeException("Index out of bounds");
            }

            arr.getRawArray().set(idx, right);
            return right;
        }

        throw new RuntimeException();//todo: exception
    }

    @Override
    public Value visitForStatement(FenixParser.ForStatementContext ctx) {
        FenixParser.ForInitContext forInit = ctx.forInit();
        FenixParser.ForConditionContext forCondition = ctx.forCondition();
        FenixParser.ForIncrementContext forIncrement = ctx.forIncrement();

        Environment prev = env;
        env = new Environment(env);

        if (forInit != null) {
            visit(forInit);
        }

        try {
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
        } finally {
            env = prev;
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
        if (rawArgs != null) {
            {
                for (var arg : rawArgs.arg()) {
                    arguments.add(new RawArg(arg.ID().getText(), TypeUtils.getTypeFromString(arg.TYPE().getText())));
                }
            }
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
        FenixParser.ExprContext expr = ctx.expr();
        Value oldValue = visit(expr);
        Value nvalue = ValueUtils.increment(oldValue);

        if (expr instanceof FenixParser.VarContext) {
            String varName = ((FenixParser.VarContext) expr).ID().getText();
            env.assign(varName, nvalue);
        } else if (expr instanceof FenixParser.FieldAccessContext) {
            FenixParser.FieldAccessContext access = (FenixParser.FieldAccessContext) expr;
            Value v = visit(access.expr());
            String fieldName = access.ID().getText();

            if (v instanceof ObjectValue) {
                ((ObjectValue) v).getObject().getVariables().put(fieldName, nvalue);
            } else if (v instanceof SelfValue) {
                ((SelfValue) v).getSelf().getVariables().put(fieldName, nvalue);
            } else {
                throw new FenixTypeException();
            }
        } else if (expr instanceof FenixParser.IndexAccessContext) {
            FenixParser.IndexAccessContext access = (FenixParser.IndexAccessContext) expr;
            Value array = visit(access.expr(0));
            int index = visit(access.expr(1)).asInt();
            if (!(array instanceof ArrayValue)) {
                throw new FenixTypeException();
            }

            ArrayValue arr = (ArrayValue) array;
            if (index < 0 || index >= arr.getRawArray().size()) {
                throw new RuntimeException("Index out of bounds");
            }

            arr.getRawArray().set(index, nvalue);
        } else {
            throw new FenixTypeException();
        }

        return oldValue;
    }

    @Override
    public Value visitPostfixDecrement(FenixParser.PostfixDecrementContext ctx) {
        FenixParser.ExprContext expr = ctx.expr();
        Value oldValue = visit(expr);
        Value nvalue = ValueUtils.decrement(oldValue);

        if (expr instanceof FenixParser.VarContext) {
            String varName = ((FenixParser.VarContext) expr).ID().getText();
            env.assign(varName, nvalue);
        } else if (expr instanceof FenixParser.FieldAccessContext) {
            FenixParser.FieldAccessContext access = (FenixParser.FieldAccessContext) expr;
            Value v = visit(access.expr());
            String fieldName = access.ID().getText();

            if (v instanceof ObjectValue) {
                ((ObjectValue) v).getObject().getVariables().put(fieldName, nvalue);
            } else if (v instanceof SelfValue) {
                ((SelfValue) v).getSelf().getVariables().put(fieldName, nvalue);
            } else {
                throw new FenixTypeException();
            }
        } else if (expr instanceof FenixParser.IndexAccessContext) {
            FenixParser.IndexAccessContext access = (FenixParser.IndexAccessContext) expr;
            Value array = visit(access.expr(0));
            int index = visit(access.expr(1)).asInt();
            if (!(array instanceof ArrayValue)) {
                throw new FenixTypeException();
            }

            ArrayValue arr = (ArrayValue) array;
            if (index < 0 || index >= arr.getRawArray().size()) {
                throw new RuntimeException("Index out of bounds");
            }

            arr.getRawArray().set(index, nvalue);
        } else {
            throw new FenixTypeException();
        }

        return oldValue;
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

        Value value = visit(expr);
        if (value.getType() != ValueType.ARRAY) {
            throw new FenixTypeException();
        }

        Environment prev = env;
        env = new Environment(env);

        String counterName = null;
        if (ctx.index != null) {
            counterName = ctx.index.getText();
        }

        try {
            if (value.getType() != ValueType.ARRAY) {
                throw new FenixTypeException();
            }

            ArrayValue array = (ArrayValue) value;

            ValueType expectedType = null;
            if (assign.varDecl_typed() != null) {
                expectedType = TypeUtils.getTypeFromString(assign.varDecl_typed().TYPE().getText().replace("[]", ""));
            }

            if (expectedType != null && !array.getRawArray().isEmpty()) {
                String typeStr = assign.varDecl_typed().TYPE().getText();
                if (!typeStr.contains("[]")) {
                    if (array.get(0).getType() != expectedType) {
                        throw new FenixTypeException();
                    }
                }
            }

            if (counterName != null){
                env.define(counterName, new IntValue(0));
            }
            env.define(varName, new NullValue());
            int counter = 0;
            for (Value v : array.getRawArray()) {
                env.assign(varName, v);

                if (counterName != null) {
                    env.assign(counterName, new IntValue(counter));
                }
                try {
                    visit(sttmt);
                } catch (BreakException be) {
                    break;
                } catch (ContinueException ce) {
                    counter++;
                    continue;
                }
                counter++;
            }
        } finally {
            env = prev;
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
        if (ctx.step != null) {
            for (int i = v1; i < v2; i+=visit(ctx.expr(2)).asInt()) {
                valueArray.add(new IntValue(i));
            }
        } else {
            IntStream.rangeClosed(v1, v2).forEach((el) -> valueArray.add(new IntValue(el)));
        }

        return new ArrayValue(valueArray);
    }

    @Override
    public Value visitStructDeclaration(FenixParser.StructDeclarationContext ctx) {
        String name = ctx.ID().getText();
        if (env.structureExists(name)) {
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
                    if (member.functionTemplate().rawArgs() != null){
                        for (var arg : member.functionTemplate().rawArgs().arg()) {
                            arguments.add(new RawArg(arg.ID().getText(), TypeUtils.getTypeFromString(arg.TYPE().getText())));
                        }
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
        if (ctx.rawArgs() != null) {
            for (var arg : ctx.rawArgs().arg()) {
                arguments.add(new RawArg(arg.ID().getText(), TypeUtils.getTypeFromString(arg.TYPE().getText())));
            }
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
    public Value visitNewExpr(FenixParser.NewExprContext ctx) {
        String structName = ctx.ID().getText();
        if (!env.structureExists(structName)) {
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
            return v1;
        }

        return v2;
    }

    @Override
    public Value visitMethodCall(FenixParser.MethodCallContext ctx) {
        Value value = visit(ctx.expr());
        String methodName = ctx.ID().getText();
        List<Value> args = new ArrayList<>();
        if (ctx.args() != null) {
            for (FenixParser.ExprContext arg : ctx.args().expr()) {
                args.add(visit(arg));
            }
        }

        if (methodName.equals("println")) {
            if (args.isEmpty()) {
                System.out.println();
            } else {
                System.out.println(args.get(0).asString());
            }
            return new NullValue();
        } else if (methodName.equals("typeof")) {
            if (args.isEmpty()) {
                throw new RuntimeException();
            }
            return new StringValue(args.get(0).getType().name());
        }

        DotOutput out = DotFunctionExecutor.execute(value, methodName, args);
        if (out.isExecuted()) {
            return out.value();
        }

        if (value instanceof ObjectValue) {
            ObjectValue obj = (ObjectValue) value;
            Struct struct = obj.getObject();
            if (!struct.getFunctions().containsKey(methodName)) {
                throw new RuntimeException();//todo: exception
            }

            FenixFunction func = struct.getFunctions().get(methodName);

            Environment prevEnv = env;
            Environment fnEnv = new Environment(env);
            for (int i = 0; i < args.size(); i++) {
                RawArg expected = func.getArgs().get(i);
                if (args.get(i).getType() != expected.type()) {
                    throw new FenixTypeException();//todo: exception(special)
                }
                fnEnv.define(expected.name(), args.get(i));
            }
            fnEnv.define("self", new SelfValue(struct));

            env = fnEnv;
            try {
                visit(func.getBody());
            } catch (ReturnException e) {
                if (e.getReturned().getType() != func.getReturnType()) {
                    throw new FenixTypeException();
                }
                return e.getReturned();
            } finally {
                env = prevEnv;
            }
            return new NullValue();
        }

        throw new FenixTypeException();
    }

    @Override
    public Value visitIndexAccess(FenixParser.IndexAccessContext ctx) {
        Value array = visit(ctx.expr(0));
        Value idx = visit(ctx.expr(1));

        if (idx.getType() != ValueType.INT) {
            throw new FenixTypeException();
        }

        int index = idx.asInt();
        if (!(array instanceof ArrayValue)) {
            throw new FenixTypeException();
        }

        ArrayValue arr = (ArrayValue) array;
        if (index < 0 || index >= arr.getRawArray().size()) {
            throw new RuntimeException("Index out of bounds");
        }

        return arr.get(index);
    }

    @Override
    public Value visitFieldAccess(FenixParser.FieldAccessContext ctx) {
        Value value = visit(ctx.expr());
        String fieldName = ctx.ID().getText();

        if (value instanceof ObjectValue) {
            ObjectValue obj = (ObjectValue) value;
            Value field = obj.getObject().getVariables().get(fieldName);
            if (field == null) {
                throw new FenixVariableNotDefinedException(fieldName);
            }

            return field;
        }

        if (value instanceof SelfValue) {
            Struct s = ((SelfValue) value).getSelf();
            Value field = s.getVariables().get(fieldName);
            if (field == null) {
                throw new FenixVariableNotDefinedException(fieldName);
            }

            return field;
        }

        throw new FenixTypeException();
    }
}
