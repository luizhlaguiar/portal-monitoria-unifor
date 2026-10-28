import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://quhrupgfkuytderogsts.supabase.co';
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF1aHJ1cGdma3V5dGRlcm9nc3RzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI0MzM4NjIsImV4cCI6MjEwODAwOTg2Mn0.F8YmoBIXDh1bP0GkaleJWNwZW301kBuTx8cVn1O83EU';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

export const storageService = {
  /**
   * Faz upload de um arquivo para o bucket 'monitoria-assets'
   * @param file O arquivo a ser enviado
   * @param folder A pasta dentro do bucket (ex: 'atividades', 'forum', 'chat')
   * @returns A URL pública do arquivo enviado
   */
  async uploadFile(file: File, folder: string): Promise<string> {
    try {
      console.log(`Iniciando upload de: ${file.name} para a pasta: ${folder}`);

      // Gera um nome único para o arquivo para evitar sobrescrita
      const fileExt = file.name.split('.').pop();
      const fileName = `${Math.random().toString(36).substring(2)}-${Date.now()}.${fileExt}`;
      const filePath = `${folder}/${fileName}`;

      console.log(`Caminho do arquivo no Supabase: ${filePath}`);

      const { data, error } = await supabase.storage
        .from('monitoria-assets')
        .upload(filePath, file);

      if (error) {
        console.error('Erro retornado pelo Supabase Storage:', error);
        throw error;
      }

      console.log('Upload concluído com sucesso. Gerando URL pública...');

      // Obtém a URL pública do arquivo
      const { data: { publicUrl } } = supabase.storage
        .from('monitoria-assets')
        .getPublicUrl(filePath);

      console.log('URL pública gerada:', publicUrl);
      return publicUrl;
    } catch (error) {
      console.error('Erro crítico no upload do arquivo:', error);
      throw new Error(`Falha ao fazer upload do arquivo: ${error instanceof Error ? error.message : 'Erro desconhecido'}`);
    }
  }
};
