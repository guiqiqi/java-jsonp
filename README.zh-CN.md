## Java JSON Parser

这是一个基于 Java 21 的 JSON 解析器项目。它提供了一种方便的方式来编码、解析和处理 JSON 数据。

支持的功能有：
- 编码 JSON 数据：该项目支持将 Java 对象编码为 JSON 字符串；
- 解析 JSON 数据：该项目支持将 JSON 字符串解析为 Java 对象，方便在代码中进行处理和操作；
- 支持基本数据类型：该项目支持处理 JSON 中的字符串、数字、布尔值和空值等基本数据类型；
- 支持复杂数据结构：该项目支持处理 JSON 中的对象和数组，允许嵌套和多层结构；
- 灵活的数据访问：该项目支持提供了一套简单而灵活的 API，可以方便地访问和操作解析后的 JSON 数据。

以下是一个简单的示例，展示了如何使用该解析器编码/解析 JSON 数据：

```java
import jsonp.*;

public class App {
    public static void main(String[] args) {
        List<Map<String, Integer>> data = List.of(Map.of("A test", 1, "B", 2), Map.of("C", 3));
        System.out.println("Raw: " + data);
        String encoded = Encoder.encode(data);
        Decoder decoder = new Decoder();
        System.out.println("Encoded: " + encoded);
        JsonObject decoded = decoder.decode(encoded);
        System.out.println("Decoded: " + decoded);
    }
}
```

### 编码器 Encoder

默认情况下，编码器支持的数据类型有：

- Java 空引用：`null`
- Java 基础数据类型：`byte, short, int, long, float, double, boolean, char`
- Java Boxed 类型：`Number, Boolean, Character, String`
- Java 容器类型：`List<?>, Map<String, ?>`

当然，你可以给自己的类实现自己的编码方法，只需要使用 `Encoder.register` 注册编码器：

```java
class Foo {
    public String name;
    public String ID;
}
Encoder.register(Foo.class, obj -> {
    Foo foo = (Foo) obj;
    return Encoder.encode(Map.of("name", foo.name, "ID", foo.ID));
});
```

注意，在编码函数中得到的是一个 `Object` 对象，你需要对其进行类型转换，最终返回编码后的字符串（当然，你可以对类属性递归地使用支持的编码器得到 JSON 字符串）。

### 解码器 Decoder

为了使用解码器，你需要实例化一个 `jsonp.decoder.Decoder` 对象，并将待解码的内容传入。

所有解码后得到的结果都是一个 `JsonObject` 对象（包括容器类型内的内容），根据其实际内容的不同，在其 `JsonObject.object` 内存储着实际的内容，你可以使用 `as` 方法将其转换为你认为它实际存在的类型。所以，除了基本类型以外，你还可能得到下面这样的容器类型：

- `List<JsonObject>` - 这对应着 JSON 的 Array；
- `Map<String, JsonObject>` - 这对应着 JSON 的 Map；

如果你需要访问容器类型内的元素，不要忘记将其转换为你需要使用的类型，就像这样：

```Java
List<Integer> items = List.of(1, 2, 3, 4, 5);
String encoded = Encoder.encode(items);
Decoder decoder = new Decoder();
List<JsonObject> decoded = decoder.decode(encoded);
for (JsonObject obj : decoded) {
    Integer item = obj.as();
    System.out.println("%d", item);
}
```

### 实现

项目主要由四个部分组成：基于 NFA 的简单正则表达式引擎、Tokenizer、Parser 以及 Encoder。

#### NFA 与正则表达式

构成 JSON 字符串的基本元素可以使用简单的正则表达式描述。例如，对于语法接受的合法数字来说，它的正则表达式看起来像是这样：

```js
-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?
```

于是，作为整个 Decoder 实现的第一步，我们需要实现一个“迷你版”的正则表达式引擎，它将告诉我们读入的字符串的每个部分是否是 JSON 语法元素中的一部分。

对于正则表达式来说，其构成的基本元素有这些：

- `Epsilon` - 这将允许一个空元素的出现
- `a|b` - 这将允许引擎接受 `a` 或者 `b` 中的任何一个元素
- `ab` - 将将告诉引擎我们希望看到 `a` 后面紧跟着 `b` 一起出现
- `a*` - 这又被称为 Klenne 闭包，它允许 `a` 出现任意次数（包括 0 次）

是的，这就是正则表达式基本的构成元素。至于 `+, ?, {n, m}, ...` 等等的这些语法，都可以看成是上面这些基本元素的组合（你可以叫它们 Syntax Sugar）。

上面这些元素（以及语法糖）都在 `jsonp.regex` 中得到了。

剩下的任务就是将正则表达式解析为一个 NFA，这样，我们就可以用来识别读入的字符串了。很幸运，Thompson 算法给出了一种根据这些基本元素构造 NFA 的办法。在 `jsonp.automata.NFA` 中，我实现了这个算法。

事实上，还可以将 NFA 转换为等价的 DFA（Subset Construction 算法可以帮助我们做到这一点）。它将在运行时更加高效，但是作为一个入门版本的 JSON 解析器，NFA 就足够了。

#### NFA 与 Tokenizer

在得到了 JSON 语法的每个基本元素的 NFA 之后，我们还需要编写一个 Tokenizer 将其连接起来，用于处理最终用户的输入。事实上这很简单：我们只需要构造一个开始状态，并将这些基本元素的开始状态使用空转移（也就是之前说的 Epsilon 元素）连接起来 —— 这样在 NFA 等待输入时，我们可以看作所有基本元素的输入状态都在等待被检查。

为了确认当前的输入是否到达尽头，我们还需要标记所有基本元素的终止状态。这样，当 Tokenizer 发现没有任何的状态可以接受当前的输入时，它就会停下来回头看一下，最后一次有元素接受的状态是什么 —— 这也就得到了解析后的基础元素。

对于 JSON 语法来说，其 Tokenizer 对应的 NFA 是这样的（你页可以使用 `NFA.draw` 在控制台打印出来看看，这是一种不错的 DEBUG 方式）：

![NFA](https://github.com/guiqiqi/java-jsonp/blob/master/README.assets/NFA.png?raw=true)

其实除了数字部分的处理以外，都还挺简单的。

这一部分代码的实现主要在 `jsonp.decoder.Lexer` 中。

#### Parser

在得到了 Tokenizer 处理过后基本元素（它们其实有个名字叫做 Token），我们需要检查它们的组合方式是否符合 JSON 的语法。JSON 的语法（文法）相对比较简单，它可以用 BNF 范式表达：

```javascript
<json> ::= <primitive> | <container>
<primitive> ::= <number> | <string> | <boolean>
<container> ::= <object> | <array>
<array> ::= '[' [ <json> *(', ' <json>) ] ']'
<object> ::= '{' [ <member> *(', ' <member>) ] '}'
<member> ::= <string> ': ' <json>
```

这看起来非常直观。不仅如此，它还是一个上下文无关的文法。为了让其更方便使用代码实现，我们将其转换为另一个等价的版本（这里使用乔姆斯基范式表达）：

```javascript
obj -> { members }
members -> pair members' | eps
members' -> , pair members' | eps
pair -> string : value
array -> [ elem ]
elem -> value elem' | eps
elem' -> , value elem' | eps
value -> obj | array | number | string | true | false | null
```

这个版本和之前的版本有什么不一样？我们将其转换为了 LL(1) 文法。这有什么好处呢？事实上，这意味着该文法可以使用 LL(1) 的解析器来进行解析。通常来说，有两种方法解析这样的文法：递归下降法以及 LL(1) 表驱动法，而前者相对简单，于是在 `jsonp.decoder.Parser` 中，我使用递归下降解析了上一步得到的 Token 流。

至此，Decoder 的实现基本就完成了。

#### Encoder 

相对于 Decoder 来说，Encoder 的实现要简单得多:

```java
public static String encode(Object obj) {
    if (obj == null)
        return "null";
    Class<?> type = obj.getClass();
    Function<Object, String> encoder = Encoders.get(type);
    if (encoder != null)
        return encoder.apply(obj);
    for (Map.Entry<Class<?>, Function<Object, String>> entry : Encoders.entrySet()) {
        if (entry.getKey().isAssignableFrom(type)) {
            return entry.getValue().apply(obj);
        }
    }
    return obj.toString();
}
```

`Encoders` 是一个静态类变量，它维护了一个类到其 encoder 函数的映射。`encoder` 函数每次从江 `Encoders` 里面查找当前的对象有没有给定的 encoder 函数 —— 如果有的话就直接使用它并返回 encode 之后的 JSON 字符串；如果没有的话，它会尝试看看有没有那个类的 Assignable 类有 encoder 函数，如果有的话就调用它。而对于完全“不认识”的类，它会尝试直接调用 `toString` 方法来做最后的“补救” —— 当然，这样并不能保证 encode 是正确的，所以，对于自定义的类，你需要调用 `Encoder.register` 来注册自己的 encoder。

这基本就是全部的实现了。