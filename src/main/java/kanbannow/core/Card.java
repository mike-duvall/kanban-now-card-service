package kanbannow.core;

public class Card {

    private long id;
    private String cardText;

    public Card() {

    }


    public void setId(long id) {
        this.id = id;
    }



    public long getId() {
        return id;
    }


    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }
}
