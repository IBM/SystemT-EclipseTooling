grammar AlgebraSimple;

options {
  language = Java;
}

@header {
package com.ibm.biginsights.textanalytics.aql.antlr;
  
  import java.util.Map;
  import java.util.HashMap;
}

@members {
Map<String, Integer> memory = new HashMap<String, Integer>();
}

@lexer::header {
package com.ibm.aql.antlr;
}

// START:stat

prog
  :
  stat+
  ;

stat
  :
  expr NEWLINE 
               {
                System.out.println($expr.value);
               }
  | ID '=' expr NEWLINE 
                        {
                         this.memory.put($ID.text, $expr.value);
                         System.out.println($ID.text + " = " + $expr.value);
                        }
  | NEWLINE
  ;
// END:stat

// START:expr

expr returns [int value]
  :
  a=multExpr 
             {
              $value = $a.value;
             }
  (
    (
      '+' b=multExpr 
                     {
                      $value = $a.value + $b.value;
                     }
      | '-' b=multExpr 
                       {
                        $value = $a.value - $b.value;
                       }
    )
  )*
  ;

multExpr returns [int value]
  :
  a=atom 
         {
          $value = $a.value;
         }
  ('*' a=atom 
              {
               $value *= $a.value;
              })*
  ;

atom returns [int value]
  :
  INT 
      {
       $value = Integer.parseInt($INT.text);
      }
  | ID 
       {
       System.out.println("WTF?");
        if (this.memory.containsKey($ID.text)) {
        	$value = this.memory.get($ID.text);
        	System.out.println("Variable access: " + $ID.text + " = " + $value);
        } else {
        	System.err.println("Unknown variable: " + $ID.text);
        }
       }
  | '(' expr ')' 
                 {
                  $value = $expr.value;
                 }
  ;
// END:expr

// START:tokens

ID
  :
  (
    'a'..'z'
    | 'A'..'Z'
  )+
  ;

INT
  :
  '0'..'9'+
  ;

NEWLINE
  :
  '\r'? '\n'
  ;

WS
  :
  (
    ' '
    | '\t'
    | '\n'
    | '\r'
  )+
  
   {
    skip();
   }
  ;
