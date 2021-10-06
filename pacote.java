import java.io.Serializable;

/* Desenvolvido por Wesley Axel de Barros - 2020. */
public class pacote implements Serializable{
	
	/* clase pacote
	possui tres atributos
	um inteiro de numero de sequencia
	um char para o caracter que será enviado
	um booleano para dizer se é o ultimo pacote da mensagem ou não.*/
	
    private int seq;
    private char data;
    private boolean ultimo;
    
    public pacote() { /*construtor basico que inicia com valores neutros e vazios*/
    	this.setData(' ');
    	this.setSeq(0);
    	this.setUltimo(false);
    }
    
    public pacote(int seq){ /* construtor para apenas um numero de sequencia*/
        this.setSeq(seq);
    }
    
    public boolean isUltimo() {
		return ultimo;
	}

	public void setUltimo(boolean ultimo) {
		this.ultimo = ultimo;
	}

    public char getData() {
		return data;
	}

	public void setData(char data) {
		this.data = data;
	}

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

}
