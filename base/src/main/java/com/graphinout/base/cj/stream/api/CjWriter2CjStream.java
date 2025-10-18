package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.JsonException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class CjWriter2CjStream implements ICjWriter {

    final ICjStream cjStream;

    public CjWriter2CjStream(ICjStream cjStream) {this.cjStream = cjStream;}

    @Override
    public void arrayEnd() throws JsonException {

    }

    @Override
    public void arrayStart() throws JsonException {

    }

    @Override
    public void baseUri(String baseUri) {

    }

    @Override
    public void direction(CjDirection direction) {

    }

    @Override
    public void documentEnd() throws JsonException {

    }

    @Override
    public void documentStart() throws JsonException {

    }

    @Override
    public void edgeEnd() {

    }

    @Override
    public void edgeStart() {

    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {

    }

    @Override
    public void endpointEnd() {

    }

    @Override
    public void endpointStart() {

    }

    @Override
    public void graphEnd() throws CjException {

    }

    @Override
    public void graphStart() throws CjException {

    }

    @Override
    public void connectedJsonStart() {

    }

    @Override
    public void connectedJson__canonical(boolean b) {

    }

    @Override
    public void connectedJson__versionDate(String s) {

    }

    @Override
    public void connectedJson__versionNumber(String s) {

    }

    @Override
    public void connectedJsonEnd() {

    }

    @Override
    public void id(String id) {

    }

    @Override
    public void jsonDataEnd() {

    }

    @Override
    public void jsonDataStart() {

    }

    @Override
    public void labelEnd() {

    }

    @Override
    public void labelEntryEnd() {

    }

    @Override
    public void labelEntryStart() {

    }

    @Override
    public void labelStart() {

    }

    @Override
    public void language(String language) {

    }

    @Override
    public void listEnd(CjType cjType) {

    }

    @Override
    public void listStart(CjType cjType) {

    }

    @Override
    public void nodeEnd() {

    }

    @Override
    public void nodeId(String nodeId) {

    }

    @Override
    public void nodeStart() {

    }

    @Override
    public void objectEnd() throws JsonException {

    }

    @Override
    public void objectStart() throws JsonException {

    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {

    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {

    }

    @Override
    public void onBoolean(boolean b) throws JsonException {

    }

    @Override
    public void onDouble(double d) throws JsonException {

    }

    @Override
    public void onFloat(float f) throws JsonException {

    }

    @Override
    public void onInteger(int i) throws JsonException {

    }

    @Override
    public void onKey(String key) throws JsonException {

    }

    @Override
    public void onLong(long l) throws JsonException {

    }

    @Override
    public void onNull() throws JsonException {

    }

    @Override
    public void onString(String s) throws JsonException {

    }

    @Override
    public void portEnd() {

    }

    @Override
    public void portId(String portId) {

    }

    @Override
    public void portStart() {

    }

    @Override
    public void value(String value) {

    }

}
