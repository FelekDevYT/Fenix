package me.felek.fenix;

import me.felek.fenix.exceptions.FenixInvalidVariableTypeException;
import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.exceptions.FenixVariableNotDefinedException;
import me.felek.fenix.exceptions.handled.BreakException;
import me.felek.fenix.exceptions.handled.ContinueException;
import me.felek.fenix.exceptions.handled.ReturnException;
import me.felek.fenix.func.RawArg;
import me.felek.fenix.func.FenixFunction;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;
import me.felek.fenix.type.impl.*;
import me.felek.fenix.utils.ComparisonUtils;
import me.felek.fenix.utils.LogicalUtils;
import me.felek.fenix.utils.TypeUtils;
import me.felek.fenix.utils.ValueUtils;
import me.felek.fenix.variable.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        return new StringValue(ctx.STRING().getText().substring(1, ctx.STRING().getText().length()-1));
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
        Value value = visit(ctx.expr());

        if (value.getType() == ValueType.INT) {
            int index = value.asInt();

            Value rawArray = env.get(name);
            if (rawArray == null) {
                throw new FenixVariableNotDefinedException(name);//todo: exception
            }

            if (!(rawArray instanceof ArrayValue)) {
                throw new RuntimeException();
            }
            ArrayValue array = (ArrayValue) rawArray;
            if (array.getRawArray().size() <= index) {
                throw new RuntimeException();//todo: exception
            }

            return array.get(index);
        } else {
            throw new RuntimeException();//todo: exception here
        }
    }

    @Override
    public Value visitCall(FenixParser.CallContext ctx) {
        Environment previousEnv = env;

        if (ctx.ID().getText().equals("println")) {
            System.out.println(visit(ctx.args().expr(0)).asString());
            return new NullValue();
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
        Value value = visit(ctx.expr());
        FenixParser.StatementContext statement = ctx.statement(0);
        FenixParser.StatementContext elseSttmt = ctx.elseSttmt;

        if (value.asBool()) {
            visit(statement);
        } else if (elseSttmt != null) {
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
        Value value = visit(ctx.expr().get(1));

        if (ctx.arr != null) {
            varName = ctx.arr.getText();
            int i = visit(ctx.expr(0)).asInt();

            env.assignArray(varName, i, value);
            return value;
        }

        env.assign(varName, value);
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
        String name = ctx.ID().getText();
        FenixParser.RawArgsContext rawArgs = ctx.rawArgs();
        //todo: add return value here please
        FenixParser.StatementContext block = ctx.statement();
        ValueType returnType = ValueType.NULL;
        if (ctx.TYPE() != null) {
            returnType = TypeUtils.getTypeFromString(ctx.TYPE().getText());
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
}
