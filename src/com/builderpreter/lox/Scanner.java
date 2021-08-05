package com.builderpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.builderpreter.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

//    Reserved keywords
    static{
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }
    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
//            set the starting lexeme to the current
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        char c = advance();
        switch(c){
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
//                Inline commments
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                }
//                Block comments
                else if(match('*')){
                    blockComments();
                }
                else{
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }
                else{
                    Lox.error(line, "Unexpected character");
                }
                break;
        }
    }

//    Helper functions
//    Consume all of the identifier lexeme and add it as a token
    private void identifier(){
        while(isAlphanumeric(peek())) advance();
//        Check if scanned token matches reserved keywords
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }
//    Check if character is alphanumeric
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }
    private boolean isAlphanumeric(char c){
        return isAlpha(c) || isDigit(c);
    }
//    Consume number strings
    private void number(){
        while(isDigit(peek())) advance();

//        Check for decimal points
        if(peek() == '.' && isDigit(peekNext())){
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
//    Block comments
    private void blockComments(){
        int nesting = 1;
        while(nesting > 0){
            if(peek() == '/' && peekNext() == '*') nesting++;
            if(peek() == '*' && peekNext() == '/') nesting--;
            if(peek() == '\n') line++;
            if(peek() == '\0') Lox.error(line, "Unterminated block comment");
            advance();
        }
        if(advance() == '*') advance();
    }
//    Consume string literals
    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            Lox.error(line, "Unterminated string.");
            return;
        }

//        Consume the closing "
        advance();

//        Trim the quotes and store the raw literal to token
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }
//    Check if the character matches with the next character
    private boolean match(char expected){
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return  false;

        current++;
        return true;
    }
//    Return next character from the character currently being checked
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }
//    Look ahead another character after peek
    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
//    Check if the character is a number
    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

//    Check if at the end of code
    private boolean isAtEnd(){
        return current >= source.length();
    }
//    Consume the current character
    private char advance(){
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }
//    Add token object
    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
