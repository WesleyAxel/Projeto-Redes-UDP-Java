import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import org.apache.commons.lang3.SerializationUtils;

/* Desenvolvido por Wesley Axel de Barros - 2020. */
public class servidor {
    public static void main(String[] args) {
    	Scanner in_t = new Scanner(System.in); /* scanner para inteiros */
    	Scanner in = new Scanner(System.in);  /* scanner para char  */
    	/*É perguntado porta que será iniciado o datagramsocket com base no ip local da maquina*/
        System.out.println("Servidor Exercicio Progamatico");
        System.out.println("Digite o numero da porta para iniciar o servidor");
        int porta = in_t.nextInt();
        System.out.println("Iniciando Servidor");
        try {
        	/* tenta iniciar o servidor inicializando um socket com a porta e o ip local da maquina para envio e recebimento*/
        	DatagramSocket servidor = new DatagramSocket(porta,InetAddress.getLocalHost());
            /* e se inicalizado imprime as informações do servidor */
        	System.out.println("Servidor inicializado");
            System.out.println("Informacoes servidor:");
            System.out.print("I.P:");
            String localIP = servidor.getLocalAddress().getHostAddress();
            System.out.println(localIP);
            System.out.println("Porta: " + servidor.getLocalPort());
            /*buffer criado para receber uma mensagem de ate 1000 caracteres. */
            pacote[] aReceber = new pacote[1000]; 
            for(int z=0; z<1000 ; z++) {
            	aReceber[z] = new pacote();
            }
            /* flags e incrementadores de inicio*/
            int i=0;
            int primeiroSeq = 0;
            boolean finaliza = false;
            while(true){
            	byte[] dataRecebida = new byte[65535]; /* quantidade maxima de bytes que um pacote pode ter */
            	DatagramPacket pack = new DatagramPacket(dataRecebida, dataRecebida.length); /*Receber pacote contendo uma letra*/
    			try{
    				if(i==0){
    					/* se é o primeiro pacote o servidor fica aguardando até receber algo*/
                		System.out.println("Aguardando pacote...");
                	}
    				else if(i>0){
    					/* se já recebeu o primeiro ele aguarda 10 segundos e lança uma excecao caso o temporizador esgote */
    					servidor.setSoTimeout(10000);
    				}
	            	servidor.receive(pack); /*comando para receber pacotes*/
	    			pacote novo = SerializationUtils.deserialize(pack.getData()); /* deserializa o pacote */
	                System.out.println("Pacote com numero de sequencia n " + novo.getSeq() + " recebido.");
	    			if(i==0){ /*se for o primeiro pacote ele n�o vai verificar duplicidade */
	    				/* adicionando no buffer e incrementando quantidades*/
                        primeiroSeq = novo.getSeq();
	    				aReceber[i] = novo;
	    				i++;
	    			}
	    			else if(novo.getSeq() > aReceber[i-1].getSeq() && novo.getSeq() == aReceber[i-1].getSeq()+1 ){ /* verifica se é o proximo pacote na sequencia*/
	    				/* adicionando no buffer e incrementando quantidades*/
	    				aReceber[i] = novo;
	    				i++;
                        if(novo.isUltimo()==true){ /* se é o ultimo pacote finaliza*/
                            finaliza=true;
                        }
	    			}
	    			else if(novo.getSeq() == primeiroSeq) {
	    				/* se o pacote recebido é igual ao primeiro pacote, imprime mensagem*/
	    				System.out.println("Possivel mensagem duplicada");
	    			}
	    			else {
	    				if(novo.isUltimo()==true){
                            finaliza=true; /* se é o ultimo pacote finaliza*/
                        }
	    				/* se não é o primeiro, não é o seguinte, então está fora de ordem*/
	    				System.out.println("Pacote recebido fora de ordem");
	    			}
	    			/* processo de enviar ack de recebimento de pacote*/
	    			pacote ack = new pacote(novo.getSeq());  /* cria um pacote ack apenas com numero de sequencia */
	    			byte[] mandar = SerializationUtils.serialize((Serializable) ack); /*serializa em bytes*/
	                DatagramPacket pack2 = new DatagramPacket(mandar, mandar.length, pack.getAddress(), pack.getPort()); /*cria um datagram packet com o endereço do cliente*/
	                servidor.send(pack2); /*envia o ack*/
	                /*ver se o ultimo pacote foi recebido*/
	    			if(finaliza==true){ 
	    				/* imprime a mensagem e finaliza*/
	    				System.out.println("Conexao finalizada");
	    				String mensagem = reformulaString(aReceber);
	    				/*pergunta se quer continuar recebendo, se sim, limpa o buffer, os incrementadores, as flags e fica em aguardo*/
	    				System.out.println("Mensagem do cliente: " + mensagem); 
	    				System.out.println("Receber nova mensagem? Y/N apenas");
	    				String op;
	    				boolean opt = false;
	    				boolean loop = false;
	    				do {
	                        op = in.nextLine();
		    				switch (op){
		    					case "N":{
		    						opt = true;
		    						loop = true;
		    						break;
		    					}
		    					case "Y":{
		    						opt = false;
		    						aReceber = limpaBuff(aReceber);
		    						i=0;
		    						loop = true;
                                    finaliza = false;
		    						break;
		    					}
		    					default:{
		    						System.out.println("Opcao nao reconhecida");
		    						break;
		    					}
		    				}
	    				}while(loop==false);
	    				if(opt == true) {
	    					System.out.println("Finalizando Servidor");
	    					break;
	    				}
	    			}
    			}catch(SocketTimeoutException e) {
    				/* excecao lançada caso o pacote seguinte do primeiro demore mais do que 10 segundos para chegar*/
    				System.out.println("Possivel pacote lento...");
    			}
            }
            /* finaliza servidor*/
            servidor.close();
        }catch (IOException ex) {
        	/* excecao lançada caso não seja possivel iniciar o servidor*/
            System.out.println("Nao foi possivel inicializar servidor");
            System.out.println(ex.getMessage());
        }
    }
    
    public static pacote[] limpaBuff(pacote[] aReceber) {
    	/* metodo para limpar buffer*/
		for(int i=0; i<aReceber.length; i++) {
			aReceber[i] = new pacote();
		}
		return aReceber;
	}

	public static String reformulaString(pacote[] novo) {
		/* metodo para reformular string do buffer*/
    	StringBuilder m = new StringBuilder();
    	for(int i=0; i<novo.length ; i++) {
    		m.insert(i, novo[i].getData());
    	}
    	String mensagem = m.toString();
    	return mensagem;
    }
    
}

