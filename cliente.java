import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.SerializationUtils;

/* Desenvolvido por Wesley Axel de Barros - 2020. */
public class cliente {
    
    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);  /* Scanner para IP e opcao */
        Scanner inINT = new Scanner(System.in); /* Scanner para porta */
       
        /* atributos e flags de inicio*/
        boolean tentativa = true;               
        String op;       
        int porta;       
        String ipstring;       
        
        System.out.println("EXERCICIO PROGAMATICO");
        
        while(tentativa == true){
        	/* é perguntado as informações do servidor e armazenado*/
            System.out.println("Digite o I.P. do servidor");
            System.out.println("Digite nesse formato xxx.xxx.xxx.xxx");
            ipstring = in.nextLine();
            System.out.println("Digite a porta do servidor");
            porta = inINT.nextInt();  
            try {
            	InetAddress ip = InetAddress.getByName(ipstring);
            	/* é criado um socket para envio e recebimento*/
                DatagramSocket servidor = new DatagramSocket(porta,InetAddress.getLocalHost());
                do{
                	/* impresso as opções de envio de mensagem*/
                    System.out.println("Escolha uma das opções ");
                    System.out.println("1 - Mensagem Normal");
                    System.out.println("2 - Mensagem Lenta");
                    System.out.println("3 - Mensagem perdida");
                    System.out.println("4 - Mensagem Desorganizada");
                    System.out.println("5 - Mensagem Duplicada");
                    System.out.println("6 - Sair");
                    op = in.nextLine();
                    /* flags de inicio*/
                    int tamanho_janela = 4;	
                    int ackesperado = 0;
                    int ultimoseq = 0;
                    int ultimopossivel = 0;
                    int tempo = 2000 ; //mss ou 2 segundos de tempo de espera de ack
                    switch(op){
                    	/*caso 1 mensagem normal*/
                        case "1":{
                        	/* criado buffer com a mensagem a ser enviada*/
                            pacote[] novo = criaPacote();
                            /* flags para janela deslizante*/
                            ultimoseq = novo[0].getSeq();
                            ackesperado = novo[0].getSeq();
                            ultimopossivel = novo[novo.length-1].getSeq();
                            int i = 0;
                            int u = 0;
                            while(true){
                            	/* ETAPA DE ENVIAR DO GO BACK N*/
                            	while(ultimoseq - ackesperado < tamanho_janela && i < novo.length) { /*loop de enviar at� a janela quebrar ou o contador i que percorre o vetor de pacotes chegar ao limite*/
    	                            System.out.println("Enviando pacote " + novo[i].getSeq());
                                    byte[] mandar = SerializationUtils.serialize((Serializable) novo[i]);  /*serializa o pacote do array a ser enviado*/
    	                            DatagramPacket pack = new DatagramPacket(mandar, mandar.length, ip, porta); /* cria um datagram packet para enviar*/
    	                            ultimoseq++;
                                    servidor.send(pack); /*envia o datagrampacket*/
    	                            i++;
                                    TimeUnit.SECONDS.sleep(2); /* para garantir que haja uma ordem nos pacotes enviados, é adicionado um delay. Esse delay permite que o pacote enviado chegue ao servidor a tempo antes do proximo pacote ser enviado.*/
                            	}
                            	/* ETAPA DE RECEBER DO GO BACK N*/
                            	byte[] ackRecebido = new byte[83]; /*criado um pequeno buffer para o ack*/
                                DatagramPacket pack = new DatagramPacket(ackRecebido, ackRecebido.length); 
                                try {
                                	/* ele vai tentar receber um pacote ack, com timeout, se o timeout esgotar, entao lancado uma excecao */
                                	servidor.setSoTimeout(tempo);
                                	servidor.receive(pack);
                                	pacote ack = SerializationUtils.deserialize(pack.getData()); /*serializa o ack*/
                                	if(ack.getSeq()==ackesperado){ /* verifica o valor de seq do ack*/
                                		u++; /* indice do ackesperado incrementa */
                                		ackesperado++;
                                        if(ack.getSeq() == ultimopossivel){
                                        	/* se for o ultimo ack possivel finaliza o envio*/
                                        	System.out.println("Mensagem recebida pelo cliente.");
                                            break;
                                        }
                                	}                 
                                }catch(SocketTimeoutException e ) {
                                	/*mandar todos antes do ack
                                	 ultimo seq ultimo enviado
                                	 ack esperado é o ack que ele esperava receber */
                                	int w = u;
                                	for(int z=ackesperado ; z<ultimoseq ; z++) {
                                			/* renvia todos os pacotes */
                                            System.out.println("Renviando pacote " + novo[w].getSeq());
                                            byte[] mandar2 = SerializationUtils.serialize((Serializable) novo[w]);
                                            w++;
                                            DatagramPacket pack1 = new DatagramPacket(mandar2, mandar2.length, ip, porta);
                                            servidor.send(pack1);
                                            TimeUnit.SECONDS.sleep(1);
                                	}
                                }    
                            }
                            break;
                        }
                        case "2":{
                        	/* caso 2 mensagem lenta*/
                        	System.out.println("Mensagem lenta de 15 segundos");
                        	System.out.println("Com chance de 10% de probabilidade");
                        	pacote[] novo = criaPacote();
                            ultimoseq = novo[0].getSeq();
                            ackesperado = novo[0].getSeq();
                            ultimopossivel = novo[novo.length-1].getSeq();
                            int i = 0;
                            int u = 0;
                            while(true){
                            	while(ultimoseq - ackesperado < tamanho_janela && i < novo.length) { /*loop de enviar at� a janela quebrar ou o contador i que percorre o vetor de pacotes chegar ao limite*/
    	                            System.out.println("Enviando pacote " + novo[i].getSeq());
                                    byte[] mandar = SerializationUtils.serialize((Serializable) novo[i]);
    	                            DatagramPacket pack = new DatagramPacket(mandar, mandar.length, ip, porta);
    	                            ultimoseq++;    	                            
    	                            i++;
    	                            /* é criado uma variavel com valor aleatorio para determinar se o pacote lançado será de envio normal ou lento*/
    	                            double prob = Math.random()*100+1;
    	                            if(prob>10){
    	                            	servidor.send(pack);
    	                            	TimeUnit.SECONDS.sleep(2);
    	                            }
    	                            else if(prob<=10) {
    	                            	System.out.println("Pacote lento nº "+i);
    	                            	/*temporizador de 15 segundos até mandar o pacote*/
    	                            	TimeUnit.SECONDS.sleep(15);
    	                            	servidor.send(pack);
    	                            	TimeUnit.SECONDS.sleep(2);
    	                            }     
                            	}
                            	byte[] ackRecebido = new byte[83];
                                DatagramPacket pack = new DatagramPacket(ackRecebido, ackRecebido.length);
                                try {
                                	servidor.setSoTimeout(tempo);
                                	servidor.receive(pack);
                                	pacote ack = SerializationUtils.deserialize(pack.getData());
                                	if(ack.getSeq()==ackesperado){
                                		u++;
                                		ackesperado++;
                                        if(ack.getSeq() == ultimopossivel){
                                        	System.out.println("Mensagem recebida pelo cliente.");
                                            break;
                                        }
                                	}                 
                                }catch(SocketTimeoutException e ) {
                                	int w = u;
                                	for(int z=ackesperado ; z<ultimoseq ; z++) {
                                            System.out.println("Renviando pacote " + novo[w].getSeq());
                                            byte[] mandar2 = SerializationUtils.serialize((Serializable) novo[w]);
                                            w++;
                                            DatagramPacket pack1 = new DatagramPacket(mandar2, mandar2.length, ip, porta);
                                            servidor.send(pack1);
                                            TimeUnit.SECONDS.sleep(1);
                                	}
                                }
                                
                            }
                            break;
                        }
                        case "3":{
                        	/*caso 3 mensagem perdida */
                        	System.out.println("Mensagem perdida");
                        	System.out.println("Com chance de 10% de probabilidade");
                        	pacote[] novo = criaPacote();
                            ultimoseq = novo[0].getSeq();
                            ackesperado = novo[0].getSeq();
                            ultimopossivel = novo[novo.length-1].getSeq();
                            int i = 0;
                            int u = 0;
                            while(true){
                            	while(ultimoseq - ackesperado < tamanho_janela && i < novo.length) { /*loop de enviar at� a janela quebrar ou o contador i que percorre o vetor de pacotes chegar ao limite*/
    	                            System.out.println("Enviando pacote " + novo[i].getSeq());
                                    byte[] mandar = SerializationUtils.serialize((Serializable) novo[i]);
    	                            DatagramPacket pack = new DatagramPacket(mandar, mandar.length, ip, porta);
    	                            /* é criado uma variavel com valor aleatorio para determinar se o pacote lançado será de envio normal ou não será enviado*/
    	                            double prob = Math.random()*100+1;
    	                            if(prob>10){
    	                            	servidor.send(pack);
    	                            	TimeUnit.SECONDS.sleep(2);
    	                            	ultimoseq++;    	                            
        	                            i++;
    	                            }
    	                            else if(prob<=10) {
    	                            	/*não envia pacote*/
    	                            	System.out.println("Pacote perdido nº "+i);
    	                            	System.out.println("Não será enviado");
    	                            } 
                            	}
                            	byte[] ackRecebido = new byte[83];
                                DatagramPacket pack = new DatagramPacket(ackRecebido, ackRecebido.length);
                                try {
                                	servidor.setSoTimeout(tempo);
                                	servidor.receive(pack);
                                	pacote ack = SerializationUtils.deserialize(pack.getData());
                                	if(ack.getSeq()==ackesperado){
                                		u++;
                                		ackesperado++;
                                        if(ack.getSeq() == ultimopossivel){
                                        	System.out.println("Mensagem recebida pelo cliente.");
                                            break;
                                        }
                                	}                 
                                }catch(SocketTimeoutException e ) {
                                	int w = u;
                                	for(int z=ackesperado ; z<ultimoseq ; z++) {
                                            System.out.println("Renviando pacote " + novo[w].getSeq());
                                            byte[] mandar2 = SerializationUtils.serialize((Serializable) novo[w]);
                                            w++;
                                            DatagramPacket pack1 = new DatagramPacket(mandar2, mandar2.length, ip, porta);
                                            servidor.send(pack1);
                                            TimeUnit.SECONDS.sleep(1);
                                	}
                                }
                                
                            }
                            break;
                        }
                        case "4":{
                         	/*caso 4 mensagem desorganizada */
                        	System.out.println("Mensagem desorganizada");
                        	System.out.println("Com chance de 10% de probabilidade");
                        	pacote[] novo = criaPacote();
                            ultimoseq = novo[0].getSeq();
                            ackesperado = novo[0].getSeq();
                            ultimopossivel = novo[novo.length-1].getSeq();
                            int i = 0;
                            int u = 0;
                            while(true){
                            	while(ultimoseq - ackesperado < tamanho_janela && i < novo.length) { //loop de enviar at� a janela quebrar ou o contador i que percorre o vetor de pacotes chegar ao limite               
                            		/* é criado uma variavel com valor aleatorio para determinar se o pacote lançado será de envio normal ou se será enviado um pacote desorganizado de indice aleatorio*/
                            		double prob = Math.random()*100+1;
    	                            if(prob>10){
    	                            	System.out.println("Enviando pacote " + novo[i].getSeq());
    	                            	byte[] mandar = SerializationUtils.serialize((Serializable) novo[i]);
        	                            DatagramPacket pack = new DatagramPacket(mandar, mandar.length, ip, porta);
    	                            	servidor.send(pack);
    	                            	TimeUnit.SECONDS.sleep(2);
    	                            	ultimoseq++;    	                            
        	                            i++;
    	                            }
    	                            else if(prob<=10) {  	                            	
    	                            	System.out.println("Pacote desorganizado");
    	                            	int rand = (int) Math.random()*novo.length+1; /*variavel com valor aleatorio entre 0 o tamanho maximo do buffer contendo o array*/
    	                            	System.out.println("Será enviado pacote nº " + rand);
    	                            	byte[] mandar = SerializationUtils.serialize((Serializable) novo[rand]);
        	                            DatagramPacket pack = new DatagramPacket(mandar, mandar.length, ip, porta);
    	                            	servidor.send(pack);
    	                            	TimeUnit.SECONDS.sleep(2);
    	                            } 
                            	}
                            	byte[] ackRecebido = new byte[83];
                                DatagramPacket pack = new DatagramPacket(ackRecebido, ackRecebido.length);
                                try {
                                	servidor.setSoTimeout(tempo);
                                	servidor.receive(pack);
                                	pacote ack = SerializationUtils.deserialize(pack.getData());
                                	if(ack.getSeq()==ackesperado){
                                		u++;
                                		ackesperado++;
                                        if(ack.getSeq() == ultimopossivel){
                                        	System.out.println("Mensagem recebida pelo cliente.");
                                            break;
                                        }
                                	}                 
                                }catch(SocketTimeoutException e ) {
                                	int w = u;
                                	for(int z=ackesperado ; z<ultimoseq ; z++) {
                                            System.out.println("Renviando pacote " + novo[w].getSeq());
                                            byte[] mandar2 = SerializationUtils.serialize((Serializable) novo[w]);
                                            w++;
                                            DatagramPacket pack1 = new DatagramPacket(mandar2, mandar2.length, ip, porta);
                                            servidor.send(pack1);
                                            TimeUnit.SECONDS.sleep(1);
                                	}
                                }        
                            }
                            break;
                        }
                        case "5":{
                         	/*caso 5 mensagem duplicada */
                        	System.out.println("Mensagem duplicada");
                        	pacote[] novo = criaPacote();
                        	/* criado um pacote*/
                        	for(int duplica=0;duplica<2;duplica++) {     
                        		/* entra em um loop de um for que ira repetir duas vezes*/
                        		if(duplica==0) {
                        			/* caso seja a primeira vez o ultimo pacote não ira finalizar a etapa de recebimento do servidor*/
                        			novo[novo.length-1].setUltimo(false);
                        		}
                        		else if(duplica==1) {
                        			/*caso seja a segunda vez, o ultimo pacote ira finalizar a etapa de recebimento do servidor*/
                        			novo[novo.length-1].setUltimo(true);
                        		}
                                ultimoseq = novo[0].getSeq();
                                ackesperado = novo[0].getSeq();
                                ultimopossivel = novo[novo.length-1].getSeq();
                                int i = 0;
                                int u = 0;
                                while(true){                             
                                	while(ultimoseq - ackesperado < tamanho_janela && i < novo.length) { 
        	                            System.out.println("Enviando pacote " + novo[i].getSeq());
                                        byte[] mandar = SerializationUtils.serialize((Serializable) novo[i]);  
        	                            DatagramPacket pack = new DatagramPacket(mandar, mandar.length, ip, porta); 
        	                            ultimoseq++;
                                        servidor.send(pack); 
        	                            i++;
                                        TimeUnit.SECONDS.sleep(2); 
                                	}
                                	byte[] ackRecebido = new byte[83]; 
                                    DatagramPacket pack = new DatagramPacket(ackRecebido, ackRecebido.length); 
                                    try {
                                    	servidor.setSoTimeout(tempo);
                                    	servidor.receive(pack);
                                    	pacote ack = SerializationUtils.deserialize(pack.getData());
                                    	if(ack.getSeq()==ackesperado){ 
                                    		u++;
                                    		ackesperado++;
                                            if(ack.getSeq() == ultimopossivel){   
                                            	if(duplica==1) {
                                            		System.out.println("Mensagem recebida pelo cliente.");
                                            	}
                                                break;
                                            }
                                    	}                 
                                    }catch(SocketTimeoutException e ) {
                                    	int w = u;
                                    	for(int z=ackesperado ; z<ultimoseq ; z++) {
                                                System.out.println("Renviando pacote " + novo[w].getSeq());
                                                byte[] mandar2 = SerializationUtils.serialize((Serializable) novo[w]);
                                                w++;
                                                DatagramPacket pack1 = new DatagramPacket(mandar2, mandar2.length, ip, porta);
                                                servidor.send(pack1);
                                                TimeUnit.SECONDS.sleep(1);
                                    	}
                                    }    
                                }
                        	}
                            break;
                        }
                        case "6":{
                        	/* opção 6 - sair */
                        	/*finaliza conexão
                        	fecha o socket e sai do loop de opções*/
                            System.out.println("Finalizando conexão");
                            servidor.close();
                            tentativa = false; /* finaliza loop de conexão */
                            break;
                        }
                        default:{
                            System.out.println("Opção não reconhecida");
                            break;
                        }    
                    }
                }while(!"6".equals(op)); /* loop de opções */
            }catch (IOException ex) {
                System.out.println("Não foi possivel conectar ao servidor");
                System.out.println(ex.getMessage());
            }
        }
    }
    
    public static pacote[] criaPacote(){ /* metodo que cria um buffer de pacotes recebendo uma string digitada pelo usuario*/
        Scanner in = new Scanner(System.in);
        String mensagem;
        int seq = (int) (Math.random()*100)+1; /* gerando numero de sequencia aleatorio entre 0 e 100; */
        boolean op = false;
        do {
            System.out.println("Digite a mensagem:");
	        mensagem = in.nextLine();
	        if(mensagem.length()>1000) { /* determina tamanho de mensagem de 1000 caracteres*/
	        	System.out.println("Mensagem ultrapassou o limite (1000 caracteres)");
	        	System.out.println("Tente novamente.");
	        }
	        else {
	        	op=true;
	        }
        }while(op==false);
        pacote[] ls = new pacote[mensagem.length()];
        
        for(int i=0 ; i< mensagem.length() ; i++) { /* transformando mensagem em um array de pacotes com cada pacote contendo um caracter; */
                ls[i] = new pacote();
        	ls[i].setData(mensagem.charAt(i));
        	ls[i].setSeq(seq+i);
        	ls[i].setUltimo(false);
        	if(i==mensagem.length()-1) {
        		ls[i].setUltimo(true);
        	}
        }
        return ls; /*retorna um array que será o buffer*/
    }
    
}
